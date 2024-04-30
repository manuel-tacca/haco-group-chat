package project;

import project.CLI.CLI;
import project.Communication.Listeners.MulticastListener;
import project.Communication.Listeners.UnicastListener;
import project.Communication.Messages.*;
import project.Communication.AckWaitingLists.AckWaitingListMulticast;
import project.Communication.AckWaitingLists.AckWaitingListUnicast;
import project.Communication.NetworkUtils;
import project.Communication.MessageHandlers.MulticastMessageHandler;
import project.Communication.MessageHandlers.UnicastMessageHandler;
import project.Exceptions.*;
import project.Model.*;
import project.Communication.Sender;

import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 *  This class is the controller of the application. Everytime the command line receives an input or
 *  a listener captures a packet, data is converted into the right format and is sent to this controller.
 *  If the input is correct, the model is updated, the view is notified, and potentially a message may be
 *  sent to other peers. If something goes wrong, an exception could be thrown, causing the requested
 *  action to be canceled.
 */
public class Client {

    private final Peer myself;
    private final InetAddress broadcastAddress;
    private final UnicastListener unicastListener;
    private final List<MulticastListener> multicastListeners;
    private final Sender sender;
    private final Set<Peer> peers;
    private final Set<Room> createdRooms;
    private final Set<Room> participatingRooms;
    private Room currentlyDisplayedRoom;
    private final Set<AckWaitingListUnicast> ackWaitingListsUni;
    private final Set<AckWaitingListMulticast> ackWaitingListsMulti;

    /**
     * Builds an instance of the application's controller.
     *
     * @param username The username given by the user.
     * @throws Exception Any error that is caused by wrong input.
     */
    public Client(String username) throws Exception {
        peers = new LinkedHashSet<>();
        createdRooms = new LinkedHashSet<>();
        participatingRooms = new LinkedHashSet<>();
        multicastListeners = new ArrayList<>();

        // connects to the network
        String ip;
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), NetworkUtils.UNICAST_PORT_NUMBER);
            ip = socket.getLocalAddress().getHostAddress();
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }
        CLI.printDebug(ip);

        myself = new Peer(username, InetAddress.getByName(ip));
        unicastListener = new UnicastListener(new DatagramSocket(NetworkUtils.UNICAST_PORT_NUMBER), new UnicastMessageHandler(this));
        sender = new Sender();
        broadcastAddress = NetworkUtils.getBroadcastAddress(myself.getIpAddress());
        currentlyDisplayedRoom = null;
        this.ackWaitingListsUni = new HashSet<>();
        this.ackWaitingListsMulti = new HashSet<>();
    }

    // GETTERS

    public Room getCurrentlyDisplayedRoom(){
        return currentlyDisplayedRoom;
    }

    public Peer getPeerData(){
        return myself;
    }

    public Set<Room> getCreatedRooms() {
        return createdRooms;
    }

    public Set<Room> getParticipatingRooms() {
        return participatingRooms;
    }

    public Set<Peer> getPeers() {
        return peers;
    }

    // SPECIAL GETTERS

    public Room getRoom(String name) throws InvalidParameterException{
        Set<Room> rooms = new HashSet<>(createdRooms);
        rooms.addAll(participatingRooms);
        Optional<Room> room = rooms.stream().filter(x -> x.getName().equals(name)).findFirst();
        if(room.isPresent()){
            return room.get();
        }
        else{
            throw new InvalidParameterException("There is no room with such a name: " + name);
        }
    }

    public Room getRoom(UUID uuid) throws InvalidParameterException {
        Set<Room> rooms = new HashSet<>(createdRooms);
        rooms.addAll(participatingRooms);
        Optional<Room> room = rooms.stream().filter(x -> x.getIdentifier().equals(uuid)).findFirst();
        if(room.isPresent()){
            return room.get();
        }
        else{
            throw new InvalidParameterException("There is no room with such a UUID: " + uuid);
        }
    }

    // SETTERS

    public void setCurrentlyDisplayedRoom(Room currentlyDisplayedRoom) {
        this.currentlyDisplayedRoom = currentlyDisplayedRoom;
    }

    // PUBLIC METHODS

    public void handlePing(Peer peer) throws IOException, PeerAlreadyPresentException {
        if(!peer.getIdentifier().equals(myself.getIdentifier())) {
            Message pongMessage = new PongMessage(peer.getIpAddress(), NetworkUtils.UNICAST_PORT_NUMBER, myself, null);
            sender.sendMessage(pongMessage);
            addPeer(peer);
        }
    }

    public void handlePong(Peer peer) throws PeerAlreadyPresentException{
        addPeer(peer);
    }

    public void handleRoomMembership(Room room, UUID ackID, UUID senderID) throws Exception {

        Optional<Peer> dstPeer = room.getRoomMembers().stream().filter(x -> x.getIdentifier().equals(senderID)).findFirst();
        AckMessage ack = new AckMessage(MessageType.ACK_UNI, myself.getIdentifier(), dstPeer.isPresent()?dstPeer.get().getIpAddress():broadcastAddress , NetworkUtils.UNICAST_PORT_NUMBER, ackID);
        sender.sendMessage(ack);

        participatingRooms.add(room);
        addMulticastListener(room);

        // if some of the peers that are in the newly created room are not part of the known peers, add them
        for (Peer peer: peers){
            if (!this.peers.contains(peer)){
                // here we save the new peer, but we don't have information about its vector clock, thus is necessary a discovery
                discoverNewPeers();
            }
        }
        CLI.appendNotification(new Notification(NotificationType.SUCCESS, "You have been inserted into the room '" + room.getName() + "' (UUID: " + room.getIdentifier() + ")"));
    }

    public void handleRoomMessage(RoomTextMessage roomTextMessage) throws Exception {
        Room room = getRoom(roomTextMessage.getRoomText().roomUUID());

        Optional<Peer> dstPeer = room.getRoomMembers().stream().filter(x -> x.getIdentifier().equals(roomTextMessage.getSenderUUID())).findFirst();
        AckMessage ack = new AckMessage(MessageType.ACK_MULTI, myself.getIdentifier(), dstPeer.isPresent()?dstPeer.get().getIpAddress():broadcastAddress, NetworkUtils.UNICAST_PORT_NUMBER, roomTextMessage.getAckID());
        sender.sendMessage(ack);

        MessageCausalityStatus status = checkMessageCausality(room.getRoomVectorClock(), roomTextMessage);
        switch (status) {
            case ACCEPTED -> {
                room.addRoomText(roomTextMessage.getRoomText());
                room.updateVectorClock(roomTextMessage.getVectorClock());
                checkDeferredMessages(room);
            }
            case QUEUED -> room.getMessageToDeliverQueue().add(roomTextMessage);
            case DISCARDED -> {}
        }
    }

    public void handleDeleteRoom(UUID roomUUID, UUID ackID, UUID senderID) throws Exception {
        Optional<Room> room = participatingRooms.stream()
                .filter(x -> x.getIdentifier().equals(roomUUID)).findFirst();
        if (room.isPresent()) {
            Room roomToBeRemoved = room.get();

            Optional<Peer> dstPeer = roomToBeRemoved.getRoomMembers().stream().filter(x -> x.getIdentifier().equals(senderID)).findFirst();
            AckMessage ack = new AckMessage(MessageType.ACK_MULTI, myself.getIdentifier(), dstPeer.isPresent()?dstPeer.get().getIpAddress():broadcastAddress, NetworkUtils.UNICAST_PORT_NUMBER, ackID);
            sender.sendMessage(ack);

            participatingRooms.remove(roomToBeRemoved);
            CLI.appendNotification(new Notification(NotificationType.INFO, "The room " + roomToBeRemoved.getName() + " has been deleted."));
        }
        else {
            throw new InvalidParameterException("There is no room with such UUID.");
        }
    }

    public void handleLeaveNetwork(Peer peer, UUID ackID, UUID senderID) throws IOException{

        Optional<Peer> dstPeer = peers.stream().filter(x -> x.getIdentifier().equals(senderID)).findFirst();
        AckMessage ack = new AckMessage(MessageType.ACK_UNI, myself.getIdentifier(), dstPeer.isPresent()?dstPeer.get().getIpAddress():broadcastAddress , NetworkUtils.UNICAST_PORT_NUMBER, ackID);
        sender.sendMessage(ack);

        for (Room r : createdRooms) {
            for (Peer p : r.getRoomMembers()) {
                if (p.getIdentifier().toString().equals(peer.getIdentifier().toString())) {
                    createdRooms.remove(r);
                    CLI.appendNotification(new Notification(NotificationType.INFO, "The room "+r.getName()+" has been deleted because "+peer.getUsername()+" has left the network!"));
                    break;
                }
            }
        }

        for (Room r : participatingRooms) {
            for (Peer p : r.getRoomMembers()) {
                if (p.getIdentifier().toString().equals(peer.getIdentifier().toString())) {
                    participatingRooms.remove(r);
                    CLI.appendNotification(new Notification(NotificationType.INFO, "The room "+r.getName()+" has been deleted because "+peer.getUsername()+" has left the network!"));
                    break;
                }
            }
        }

        for (AckWaitingListMulticast awl : ackWaitingListsMulti) {
            if (awl.getAckingPeers().contains(peer)) {
                ackWaitingListsMulti.remove(awl);
            }
        }

        for (AckWaitingListUnicast awl : ackWaitingListsUni) {
            for (Message m : awl.getMessagesToResend()) {
                if (peer.getIpAddress().equals(m.getDestinationAddress())) {
                    ackWaitingListsUni.remove(awl);
                    break;
                }
            }
        }

        peers.removeIf(p -> p.getIdentifier().toString().equals(peer.getIdentifier().toString()));

        CLI.printPeers(peers);

        /*System.out.println("Created rooms peers");
        System.out.println("");
        for(Room r : createdRooms) {
            System.out.println(r.getName()+": "+ r.getRoomMembers());
        }
        System.out.println("Participating rooms peers");
        System.out.println("");
        for(Room r : participatingRooms) {
            System.out.println(r.getName()+": "+ r.getRoomMembers());
        }*/

        if (!(participatingRooms.contains(currentlyDisplayedRoom) || createdRooms.contains(currentlyDisplayedRoom))) {
            currentlyDisplayedRoom = null;
        }
    }

    public void handleAckUni(UUID ackID, UUID senderID) {

        for(AckWaitingListUnicast awl: ackWaitingListsUni) {
            if(awl.getAckID().equals(ackID)) {

                Optional<Peer> dstPeer = peers.stream().filter(x -> x.getIdentifier().equals(senderID)).findFirst();
                awl.update(dstPeer.map(Peer::getIpAddress).orElse(null));

                if (awl.getIsComplete()) {
                    ackWaitingListsUni.remove(awl); //TODO: vedi se funziona o se va spostata fuori dal for
                }

                break;
            }
        }
    }

    public void handleAckMulti(UUID ackID, UUID senderID) {

        for(AckWaitingListMulticast awl: ackWaitingListsMulti) {
            if(awl.getAckID().equals(ackID)) {
                Optional<Peer> dstPeer = peers.stream().filter(x -> x.getIdentifier().equals(senderID)).findFirst();
                awl.update(dstPeer.orElse(null));

                if (awl.getIsComplete()) {
                    ackWaitingListsMulti.remove(awl); //TODO: vedi se funziona o se va spostata fuori dal for
                }

                break;
            }
        }
    }

    public void discoverNewPeers() throws IOException{
        Message pingMessage = new PingMessage(broadcastAddress, NetworkUtils.UNICAST_PORT_NUMBER, myself, null);
        sender.sendMessage(pingMessage);
    }

    public void createRoom(String roomName, String[] peerIds) throws IOException {

        // creates the set of peer members
        Set<Peer> roomMembers = new HashSet<>();
        // adds myself to the set
        roomMembers.add(myself);

        // iterates over the set and understands which peers the user picked
        Iterator<Peer> iterator = peers.iterator();
        Set<Integer> choices = new HashSet<>();
        for(String peerId: peerIds){
            choices.add(Integer.parseInt(peerId));
        }
        int index = 1;
        while(iterator.hasNext()){
            Peer peer = iterator.next();
            if(choices.contains(index)){
                roomMembers.add(peer);
            }
            index++;
        }

        // creates the room and the associated multicast listener
        Room room = new Room(roomName, roomMembers, NetworkUtils.generateRandomMulticastAddress());
        createdRooms.add(room);
        addMulticastListener(room);

        List<Message> messagesToResend = new ArrayList<>();
        UUID ackID = UUID.randomUUID();

        for (Peer p : room.getRoomMembers()) {
            if(!p.getIdentifier().equals(myself.getIdentifier())) {
                Message roomMembershipMessage = new RoomMembershipMessage(myself.getIdentifier(), p.getIpAddress(), NetworkUtils.UNICAST_PORT_NUMBER, room, ackID);
                messagesToResend.add(roomMembershipMessage);
                sender.sendMessage(roomMembershipMessage);
            }
        }

        scheduleAckUni(ackID, messagesToResend);
    }

    public void deleteCreatedRoom(Room room) throws IOException {
        UUID ackID = UUID.randomUUID();

        Message deleteRoomMessage = new DeleteRoomMessage(myself.getIdentifier(),
                room.getMulticastAddress(), NetworkUtils.MULTICAST_PORT_NUMBER, room.getIdentifier(), ackID);

        sender.sendMessage(deleteRoomMessage);
        createdRooms.remove(room);

        Set<Peer> peers = room.getRoomMembers();
        peers.removeIf(p -> p.getIdentifier().toString().equals(myself.getIdentifier().toString()));
        scheduleAckMulti(ackID, peers, deleteRoomMessage);
    }

    public void deleteCreatedRoom(String roomName) throws InvalidParameterException, SameRoomNameException, IOException {
        List<Room> filteredRooms = createdRooms.stream()
                .filter(x -> x.getName().equals(roomName)).toList();

        int numberOfElements = filteredRooms.size();

        if (numberOfElements == 0) {
            throw new InvalidParameterException("There is no room that can be deleted with the name '" + roomName + "'");
        } else if (numberOfElements > 1) {
            throw new SameRoomNameException("There is more than one room that can be deleted with the name'" + roomName + "'", filteredRooms);
        } else {
            Room room = filteredRooms.get(0);
            deleteCreatedRoom(room);
        }
    }

    public void sendRoomText(RoomText roomText) throws IOException {

        UUID ackID = UUID.randomUUID();

        currentlyDisplayedRoom.addRoomText(roomText);
        currentlyDisplayedRoom.incrementVectorClock(myself.getIdentifier());
        Message message = new RoomTextMessage(currentlyDisplayedRoom.getRoomVectorClock(), myself.getIdentifier(),
                currentlyDisplayedRoom.getMulticastAddress(), NetworkUtils.MULTICAST_PORT_NUMBER, roomText, ackID);
        sender.sendMessage(message);

        Set<Peer> peers = currentlyDisplayedRoom.getRoomMembers();

        peers.removeIf(p -> p.getIdentifier().toString().equals(myself.getIdentifier().toString()));

        scheduleAckMulti(ackID, peers, message);
    }

    public void close() throws IOException {

        // tells every peer in the network that the user is leaving
        //Message leaveNetworkMessage = new LeaveNetworkMessage(broadcastAddress, NetworkUtils.UNICAST_PORT_NUMBER, myself, ackID);
        //sender.sendMessage(leaveNetworkMessage);

        List<Message> messagesToResend = new ArrayList<>();
        UUID ackID = UUID.randomUUID();

        for (Peer p : peers) {
            if(!p.getIdentifier().equals(myself.getIdentifier())) {
                Message leaveNetworkMessage = new LeaveNetworkMessage(p.getIpAddress(), NetworkUtils.UNICAST_PORT_NUMBER, myself, ackID);
                messagesToResend.add(leaveNetworkMessage);
                sender.sendMessage(leaveNetworkMessage);
            }
        }

        scheduleAckUni(ackID, messagesToResend);
        AckWaitingListUnicast awl = null;
        for (AckWaitingListUnicast a : ackWaitingListsUni) {
            if (a.getAckID().equals(ackID)) {
                awl = a;
                break;
            }
        }
        while (true) {
            if (awl.getIsComplete()) {
                break;
            }
        }
        // closes the sockets and the input scanner
        CLI.printDebug("sto per chiudere tutto");
        unicastListener.close();
        for(MulticastListener multicastListener: multicastListeners){
            multicastListener.close();
        }

    }

    public boolean existsRoom(String roomName) throws SameRoomNameException {
        List<Room> allRooms = new ArrayList<>();
        allRooms.addAll(participatingRooms);
        allRooms.addAll(createdRooms);
        List<Room> matchingRooms = allRooms.stream().filter(x -> x.getName().equals(roomName)).toList();

        if(matchingRooms.isEmpty()){
            return false;
        }
        else if (matchingRooms.size() > 1){
            throw new SameRoomNameException("There are " + matchingRooms.size() + " rooms with the same name.", matchingRooms);
        }

        return true;
    }

    // PRIVATE METHODS

    private void addPeer(Peer p) throws PeerAlreadyPresentException {
        for (Peer peer : this.peers) {
            if (p.getIdentifier().toString().equals(peer.getIdentifier().toString())) {
                throw new PeerAlreadyPresentException("There's already a peer with such an ID.");
            }
        }
        peers.add(p);
    }

    private void addMulticastListener(Room room) throws IOException {
        MulticastSocket multicastSocket = new MulticastSocket(NetworkUtils.MULTICAST_PORT_NUMBER);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(room.getMulticastAddress(), NetworkUtils.MULTICAST_PORT_NUMBER);
        NetworkInterface networkInterface = NetworkUtils.getAvailableMulticastIPv4NetworkInterface();
        multicastSocket.joinGroup(inetSocketAddress, networkInterface);
        multicastListeners.add(new MulticastListener(multicastSocket, new MulticastMessageHandler(this), inetSocketAddress, networkInterface));
    }

    /**
     * Method used to check if the causality between messages is respected.
     * @param message The message to analyze.
     *
     * @return ACCEPTED if the message respects the causality and thus can be processed, QUEUED if the message cannot
     * be processed because a message is missing (and we have to wait for it), DISCARDED if it's an old message that should be discarded.
     */
    private MessageCausalityStatus checkMessageCausality(Map<UUID, Integer> roomVectorClock, RoomTextMessage message) {
        for (Map.Entry<UUID, Integer> entry : message.getVectorClock().entrySet()) {
            UUID uuid = entry.getKey();
            int messageTimestamp = entry.getValue();
            int roomTimestamp = roomVectorClock.getOrDefault(uuid, 0);
            // CLI.printDebug("Message timestamp: " + messageTimestamp);
            // CLI.printDebug("Room timestamp: " + roomTimestamp);
            if ((messageTimestamp > roomTimestamp && !uuid.equals(message.getSenderUUID()))) {
                CLI.printDebug("QUEUED");
                return MessageCausalityStatus.QUEUED;
            }
            if (uuid.equals(message.getSenderUUID()) && messageTimestamp < roomTimestamp ||
                    message.getVectorClock().equals(roomVectorClock)) {
                CLI.printDebug("DISCARDED");
                return MessageCausalityStatus.DISCARDED;
            }
        }
        CLI.printDebug("ACCEPTED");
        return MessageCausalityStatus.ACCEPTED;
    }

    /**
     * Method to check and process deferred messages (i.e. messages that wait to be processed)
     *
     * @throws Exception if there is any problem when handling the message.
     */
    private void checkDeferredMessages(Room room) throws Exception {
        Iterator<RoomTextMessage> iterator = room.getMessageToDeliverQueue().iterator();
        while (iterator.hasNext()) {
            RoomTextMessage roomTextMessage = iterator.next();
            MessageCausalityStatus status = checkMessageCausality(room.getRoomVectorClock(), roomTextMessage);
            if (status.equals(MessageCausalityStatus.ACCEPTED)) {
                iterator.remove();
                handleRoomMessage(roomTextMessage);
                checkDeferredMessages(room);
            }
        }
    }

    /**
     * Method to build an AckWaitingList and start the related timer
     *
     * @param ackID unique ID of the AckWaitingList
     * @param messagesToResend list of messages to resend at timeout
     */
    private void scheduleAckUni(UUID ackID, List<Message> messagesToResend){
        AckWaitingListUnicast awl = new AckWaitingListUnicast(ackID, sender, messagesToResend);
        ackWaitingListsUni.add(awl);
        awl.startTimer();
    }

    /**
     * Method to build an AckWaitingList and start the related timer
     *
     * @param ackID unique ID of the AckWaitingList
     * @param peers set of peers who have to send their ack
     */
    private void scheduleAckMulti(UUID ackID, Set<Peer> peers, Message message){
        AckWaitingListMulticast awl = new AckWaitingListMulticast(ackID, sender, peers, message);
        ackWaitingListsMulti.add(awl);
        awl.startTimer();
    }

}
