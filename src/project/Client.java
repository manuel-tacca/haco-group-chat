package project;

import project.CLI.CLI;
import project.Communication.Listeners.Listener;
import project.Communication.Listeners.MulticastListener;
import project.Communication.NetworkUtils;
import project.Communication.PacketHandlers.MulticastPacketHandler;
import project.Communication.PacketHandlers.PacketHandler;
import project.DataStructures.MissingPeerRecoveryData;
import project.Exceptions.*;
import project.Model.Peer;
import project.Model.Room;
import project.Communication.Messages.MessageBuilder;
import project.Communication.Messages.Message;
import project.Communication.Sender;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

import static java.lang.System.out;

/** This class is the controller of the application. Everytime the command line receives an input or
 *  a listener captures a packet, data is converted into the right format and is sent to this controller.
 *  If the input is correct, the model is updated, the view is notified, and potentially a message may be
 *  sent to other peers. If something goes wrong, an exception could be thrown, causing the requested
 *  action to be canceled.
 */

public class Client {

    private final Peer myself;
    private final InetAddress broadcastAddress;
    private final PacketHandler packetHandler;
    private final List<MulticastPacketHandler> multicastPacketHandlers;
    private final Sender sender;
    private final List<Peer> peers;
    private final List<Room> createdRooms;
    private final List<Room> participatingRooms;
    private final Scanner inScanner;
    private Room currentlyDisplayedRoom;
    private final Map<String, StringBuilder> roomMessages;

    /**
     * Builds an instance of the application's controller.
     *
     * @param username The username given by the user.
     * @throws Exception Any error that is caused by wrong input.
     */
    public Client(String username) throws Exception {
        peers = new ArrayList<>();
        createdRooms = new ArrayList<>();
        participatingRooms = new ArrayList<>();
        multicastPacketHandlers = new ArrayList<>();
        inScanner = new Scanner(System.in);

        // connects to the network
        String ip;
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), NetworkUtils.UNICAST_PORT_NUMBER);
            ip = socket.getLocalAddress().getHostAddress();
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }
        CLI.printDebug(ip);

        this.myself = new Peer(username, InetAddress.getByName(ip));
        this.packetHandler = new PacketHandler(this);
        this.sender = new Sender();
        sender.sendPendingPacketsAtFixedRate(1);
        this.broadcastAddress = NetworkUtils.getBroadcastAddress(myself.getIpAddress());
        currentlyDisplayedRoom = new Room("stub", null);;
        roomMessages = new HashMap<>();
    }

    public Map<String, StringBuilder> getRoomMessagesMap() {
        return roomMessages;
    }

    public Room getCurrentlyDisplayedRoom(){
        return currentlyDisplayedRoom;
    }

    public PacketHandler getPacketHandler(){
        return packetHandler;
    }

    public Peer getPeerData(){
        return myself;
    }

    public List<Room> getCreatedRooms() {
        return createdRooms;
    }

    public List<Room> getParticipatingRooms() {
        return participatingRooms;
    }

    public List<Peer> getPeers() {
        return peers;
    }

    public Listener getListener(){
        return packetHandler.getListener();
    }

    public MulticastListener getMulticastListeners(UUID roomUUID) throws InvalidParameterException {
        List<MulticastListener> multicastListeners = new ArrayList<>();
        for(MulticastPacketHandler multicastPacketHandler: multicastPacketHandlers){
            multicastListeners.add(multicastPacketHandler.getMulticastListener());
        }
        Optional<MulticastListener> multicastListener = multicastListeners.stream().filter(x -> x.getRoomIdentifier().equals(roomUUID)).findFirst();
        if(multicastListener.isPresent()){
            return multicastListener.get();
        }
        else{
            throw new InvalidParameterException("There is no room with such a UUID: " + roomUUID);
        }
    }

    public String getMessagesForRoom(String roomID) {
        return roomMessages.getOrDefault(roomID, new StringBuilder()).toString();
    }

    public void handlePing(UUID userUUID, String username, InetAddress senderAddress) throws IOException, PeerAlreadyPresentException {
        if(!userUUID.equals(myself.getIdentifier())) {
            addPeer(new Peer(userUUID, username, senderAddress));
            Message response = MessageBuilder.pong(myself.getIdentifier(), myself.getUsername(), senderAddress, NetworkUtils.UNICAST_PORT_NUMBER);
            sender.sendPacket(response);
        }
    }

    public void handlePong(UUID userUUID, String username, InetAddress senderAddress) throws PeerAlreadyPresentException {
        addPeer(new Peer(userUUID, username, senderAddress));
    }

    public void handleRoomMembership(UUID roomUUID, String roomName, InetAddress multicastAddress, List<UUID> otherUUIDs) throws Exception {

        Room room = new Room(roomUUID, roomName, multicastAddress);
        participatingRooms.add(room);
        for(UUID uuid : otherUUIDs) {
            Optional<Peer> peer = peers.stream().filter(x -> x.getIdentifier().equals(uuid)).findFirst();
            if(peer.isPresent()) {
                room.addPeer(peer.get());
            }
            else{
                /*missingPeers.add(new MissingPeerRecoveryData(p.getIdentifier().toString(), roomId));
                findMissingPeer(senderAddress,p.getIdentifier().toString(), roomId);*/ //FIXME
            }
        }
        // TODO: aggiungere localmente su client la nuova room con la lista dei peer
    }

    private void addPeer(Peer p) throws PeerAlreadyPresentException{
        for (Peer peer : this.peers) {
            if (p.getIdentifier().toString().equals(peer.getIdentifier().toString())) {
                throw new PeerAlreadyPresentException("There's already a peer with such an ID.");
            }
        }
        peers.add(p);
    }

    public void discoverNewPeers() throws IOException{
        Message pingMessage = MessageBuilder.ping(myself.getIdentifier(), myself.getUsername(), broadcastAddress, NetworkUtils.UNICAST_PORT_NUMBER);
        sender.sendPacket(pingMessage);
    }

    public void createRoom(String roomName, String[] peerIds) throws Exception {

        Room room = new Room(roomName, NetworkUtils.generateRandomMulticastAddress());

        for(String peerId: peerIds){
            int id = Integer.parseInt(peerId);
            room.addPeer(peers.get(id - 1));
        }

        if (room.getOtherRoomMembers().isEmpty()) {
            throw new EmptyRoomException(null);
        }

        this.createdRooms.add(room);

        // 3. I send to each chosen peer a message with the information of the room and other members
        for (Peer p : room.getOtherRoomMembers()) {
            Message roomMembershipMessage = MessageBuilder.roomMembership(myself.getIdentifier(), room.getIdentifier(),
                    room.getName(), room.getMulticastAddress(), myself, room.getOtherRoomMembers(), p.getIpAddress(), NetworkUtils.UNICAST_PORT_NUMBER);
        }

        // 4. instanzia un nuovo thread che ascolti sul multicastAddress deciso dall'utente. La nuova classe roomHandler servirà a gestire i pacchetti ricevuti relativi ad ogni room
        //    + tengo una lista di roomHandlers in client. Prosegui su packetHandler.
        multicastPacketHandlers.add(new MulticastPacketHandler(this, room));
    }

    public void createRoomMembership(Peer creator, UUID roomUUID, String roomName, InetAddress multicastAddress){
        Room room = new Room(roomUUID, roomName, multicastAddress);
        CLI.appendNotification("You have been inserted in a new room by "+creator.getUsername()+"! The ID of the room is: "+roomUUID);
        try {
            room.addPeer(creator);
        }
        catch(PeerAlreadyPresentException e){
            throw new RuntimeException(e.getMessage());
        }
        participatingRooms.add(room);
    }

    public void deleteCreatedRoom(String roomName) throws InvalidRoomNameException, SameRoomNameException, IOException {
        List<Room> filteredRooms = createdRooms.stream()
                .filter(x -> x.getName().equals(roomName)).toList();

        int numberOfElements = filteredRooms.size();

        if (numberOfElements == 0) {
            throw new InvalidRoomNameException("There is no room that can be deleted with the name provided.");
        } else if (numberOfElements > 1) {
            throw new SameRoomNameException("There is more than one room that can be deleted with the name provided.", filteredRooms);
        } else {
            Room room = filteredRooms.get(0);
            for (Peer p : room.getOtherRoomMembers()) {
                /*Message roomDeleteMessage = MessageBuilder.roomDelete(getProcessID(), room.getIdentifier().toString(), p.getIpAddress(), p.getIdentifier());
                sendPacket(roomDeleteMessage);*/
            }
            createdRooms.remove(room);
        }
    }

    public void deleteCreatedRoomMultipleChoice(Room roomSelected) throws IOException {
        for (Peer p : roomSelected.getOtherRoomMembers()) {
            /*Message roomDeleteMessage = MessageBuilder.roomDelete(getProcessID(), roomSelected.getIdentifier().toString(), p.getIpAddress(), p.getIdentifier());
            sendPacket(roomDeleteMessage);*/
        }
        createdRooms.remove(roomSelected);
    }

    public void deleteRoom(String roomID) {
        Optional<Room> room = participatingRooms.stream()
                .filter(x -> x.getIdentifier().toString().equals(roomID)).findFirst();
        Room roomToBeRemoved = room.get();
        participatingRooms.remove(roomToBeRemoved);
        CLI.appendNotification("The room " + roomToBeRemoved.getName() + " has been deleted.");
    }

    public void findMissingPeer(InetAddress destinationAddress, String missingPeerID, String roomID) throws IOException {
        Optional<Room> room = participatingRooms.stream().filter(x -> x.getIdentifier().toString().equals(roomID)).findFirst();
        UUID creatorUUID;
        if(room.isPresent()){
            creatorUUID = room.get().getOtherRoomMembers().get(0).getIdentifier();
        }
        else{
            throw new RuntimeException();
        }
        /*Message request = MessageBuilder.memberInfoRequest(myself.getIdentifier().toString(), missingPeerID, roomID, destinationAddress, NetworkUtils.UNICAST_PORT_NUMBER);
        sender.sendPacket(request);*/ //FIXME
    }

    public void close() {
        sender.close();
        packetHandler.shutdown();
        multicastPacketHandlers.forEach(MulticastPacketHandler::shutdown);
        inScanner.close();
    }

    public void chatInRoom(String roomName) throws Exception {
        List<Room> allRooms = new ArrayList<>();
        allRooms.addAll(participatingRooms);
        allRooms.addAll(createdRooms);
        List<Room> matchingRooms = allRooms.stream().filter(x -> x.getName().equals(roomName)).toList();

        if(matchingRooms.isEmpty()){
            throw new InvalidRoomNameException("There's no room with such a name.");
        }
        else if (matchingRooms.size() > 1){
            throw new SameRoomNameException("There are " + matchingRooms.size() + " rooms with the same name.", matchingRooms);
        }

        this.currentlyDisplayedRoom = matchingRooms.get(0);
        boolean online = true;

        //out.println("-----"+room.getName().toUpperCase()+"-----");

        String previous_messages = getMessagesForRoom(currentlyDisplayedRoom.getIdentifier().toString());
        if (!previous_messages.isEmpty()) {
            out.println(previous_messages);
            roomMessages.remove(currentlyDisplayedRoom.getIdentifier().toString());
        }

        while (online) {
            out.println("Type your message [insert 'EXIT_ROOM' to exit]: ");
            String content = inScanner.nextLine();
            if (content.isEmpty()) {
                content = inScanner.nextLine();
            }
            else if (content.equals("EXIT_ROOM")) {
                online = false;
            } else if(!content.isEmpty()){
                for (Peer p : currentlyDisplayedRoom.getOtherRoomMembers()) {
                    /*Message message = MessageBuilder.roomMessage(currentlyDisplayedRoom.getIdentifier().toString(), myself, content, p.getIpAddress(), p.getIdentifier());
                    sender.sendPacket(message);*/ //FIXME: sequence number!!!
                }
            }
        }
    }

}
