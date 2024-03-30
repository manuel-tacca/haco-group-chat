package project;

import project.CLI.CLI;
import project.Communication.Listeners.MulticastListener;
import project.Communication.Listeners.UnicastListener;
import project.Communication.Messages.*;
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
    private final Scanner inScanner;
    private Room currentlyDisplayedRoom;
    private final Map<UUID, Integer> vectorClock;

    /**
     * Builds an instance of the application's controller.
     *
     * @param username The username given by the user.
     * @throws Exception Any error that is caused by wrong input.
     */
    public Client(String username) throws Exception {
        peers = new LinkedHashSet<>();
        createdRooms = new HashSet<>();
        participatingRooms = new HashSet<>();
        multicastListeners = new ArrayList<>();
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

        myself = new Peer(username, InetAddress.getByName(ip));
        unicastListener = new UnicastListener(new DatagramSocket(NetworkUtils.UNICAST_PORT_NUMBER), new UnicastMessageHandler(this));
        sender = new Sender();
        broadcastAddress = NetworkUtils.getBroadcastAddress(myself.getIpAddress());
        currentlyDisplayedRoom = new Room("stub", null, null); //FIXME replacement with null is now possible?
        vectorClock = new HashMap<>();
        vectorClock.put(myself.getIdentifier(), 0);

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

    public Map<UUID, Integer> getVectorClock() { return vectorClock; }

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

    public List<RoomText> getRoomMessages(String roomName) throws InvalidParameterException {
        return getRoom(roomName).getRoomMessages();
    }

    public List<RoomText> getRoomMessages(UUID roomUUID) throws InvalidParameterException {
        return getRoom(roomUUID).getRoomMessages();
    }

    // SETTERS

    public void setCurrentlyDisplayedRoom(Room currentlyDisplayedRoom) {
        this.currentlyDisplayedRoom = currentlyDisplayedRoom;
    }

    // PUBLIC METHODS

    public void handlePing(Peer peer, Map<UUID, Integer> vectorClockReceived) throws IOException, PeerAlreadyPresentException {
        if(!peer.getIdentifier().equals(myself.getIdentifier())) {
            Message pongMessage = new PongMessage(vectorClock, peer.getIpAddress(), NetworkUtils.UNICAST_PORT_NUMBER, myself);
            sender.sendMessage(pongMessage);
            addPeer(peer, vectorClockReceived);
        }
    }

    public void handlePong(Peer peer, Map<UUID, Integer> vectorClockReceived) throws PeerAlreadyPresentException, IOException {
        addPeer(peer, vectorClockReceived);
    }

    public void handleRoomMembership(Room room) throws Exception {
        participatingRooms.add(room);

        addMulticastListener(room);

        // if some of the peers that are in the newly created room are not part of the known peers, add them
        for (Peer peer: peers){
            if (!this.peers.contains(peer)){
                // here we save the new peer, but we don't have information about its vector clock, thus is necessary a discover
                discoverNewPeers();
            }
        }

        CLI.appendNotification(new Notification(NotificationType.SUCCESS, "You have been inserted into the room '" + room.getName() + "' (UUID: " + room.getIdentifier() + ")"));
    }

    public void handleRoomText(RoomText roomText) throws InvalidParameterException {
        Room room = getRoom(roomText.roomUUID());
        room.addRoomText(roomText);
    }

    public void handleDeleteRoom(UUID roomUUID) throws InvalidParameterException {
        Optional<Room> room = participatingRooms.stream()
                .filter(x -> x.getIdentifier().equals(roomUUID)).findFirst();
        if (room.isPresent()) {
            Room roomToBeRemoved = room.get();
            participatingRooms.remove(roomToBeRemoved);
            CLI.appendNotification(new Notification(NotificationType.INFO, "The room " + roomToBeRemoved.getName() + " has been deleted."));
        }
        else {
            throw new InvalidParameterException("There is no room with such UUID.");
        }
    }

    public void handleLeaveNetwork(Peer peer){
        peers.remove(peer);
        vectorClock.remove(peer.getIdentifier());
    }

    public void discoverNewPeers() throws IOException{
        Message pingMessage = new PingMessage(vectorClock, broadcastAddress, NetworkUtils.UNICAST_PORT_NUMBER, myself);
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

        // notifies the participating peers of the room creation
        incrementVectorClock();
        for (Peer p : room.getRoomMembers()) {
            if(!p.getIdentifier().equals(myself.getIdentifier())) {
                Message roomMembershipMessage = new RoomMembershipMessage(vectorClock, myself.getIdentifier(), p.getIpAddress(), NetworkUtils.UNICAST_PORT_NUMBER, room);
                sender.sendMessage(roomMembershipMessage);
            }
        }
    }

    public void deleteCreatedRoom(String roomName) throws InvalidParameterException, SameRoomNameException, IOException {
        List<Room> filteredRooms = createdRooms.stream()
                .filter(x -> x.getName().equals(roomName)).toList();

        int numberOfElements = filteredRooms.size();

        if (numberOfElements == 0) {
            throw new InvalidParameterException("There is no room that can be deleted with the name provided.");
        } else if (numberOfElements > 1) {
            throw new SameRoomNameException("There is more than one room that can be deleted with the name provided.", filteredRooms);
        } else {
            Room room = filteredRooms.get(0);
            incrementVectorClock(); // increment the vector clock because we are sending a message
            Message deleteRoomMessage = new DeleteRoomMessage(vectorClock, myself.getIdentifier(), room.getMulticastAddress(), NetworkUtils.MULTICAST_PORT_NUMBER, room.getIdentifier());
            sender.sendMessage(deleteRoomMessage);
            createdRooms.remove(room);
            // incrementVectorClock(); // increment the vector clock because we are modifying the current state
        }
    }

    public void deleteCreatedRoomMultipleChoice(Room roomSelected) throws IOException {
        incrementVectorClock(); // increment the vector clock because we are sending a message
        Message deleteRoomMessage = new DeleteRoomMessage(vectorClock, myself.getIdentifier(), roomSelected.getMulticastAddress(), NetworkUtils.MULTICAST_PORT_NUMBER, roomSelected.getIdentifier());
        sender.sendMessage(deleteRoomMessage);
        createdRooms.remove(roomSelected);
        // incrementVectorClock(); // increment the vector clock because we are modifying the current state
    }

    public void sendRoomText(RoomText roomText) throws IOException {
        currentlyDisplayedRoom.addRoomText(roomText);
        incrementVectorClock();
        Message message = new RoomTextMessage(vectorClock, myself.getIdentifier(), currentlyDisplayedRoom.getMulticastAddress(), NetworkUtils.MULTICAST_PORT_NUMBER, roomText);
        sender.sendMessage(message);
    }

    public void close() throws IOException {

        // tells every peer in the network that the user is leaving
        Message leaveNetworkMessage = new LeaveNetworkMessage(vectorClock, broadcastAddress, NetworkUtils.UNICAST_PORT_NUMBER, myself);
        sender.sendMessage(leaveNetworkMessage);

        // closes the sockets and the input scanner
        unicastListener.close();
        for(MulticastListener multicastListener: multicastListeners){
            multicastListener.close();
        }
        inScanner.close();

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

    private void addPeer(Peer p, Map<UUID, Integer> vectorClockReceived) throws PeerAlreadyPresentException, IOException {
        for (Peer peer : this.peers) {
            if (p.getIdentifier().toString().equals(peer.getIdentifier().toString())) {
                throw new PeerAlreadyPresentException("There's already a peer with such an ID.");
            }
        }
        peers.add(p);
        vectorClock.put(p.getIdentifier(), vectorClockReceived.get(p.getIdentifier()));
        // updateVectorClock(vectorClockReceived);
        // incrementVectorClock();
    }

    private void incrementVectorClock(){
        vectorClock.replace(myself.getIdentifier(), vectorClock.get(myself.getIdentifier())+1);
    }

    private void addMulticastListener(Room room) throws IOException {
        MulticastSocket multicastSocket = new MulticastSocket(NetworkUtils.MULTICAST_PORT_NUMBER);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(room.getMulticastAddress(), NetworkUtils.MULTICAST_PORT_NUMBER);
        NetworkInterface networkInterface = NetworkUtils.getAvailableMulticastIPv4NetworkInterface();
        multicastSocket.joinGroup(inetSocketAddress, networkInterface);
        multicastListeners.add(new MulticastListener(multicastSocket, new MulticastMessageHandler(this), inetSocketAddress, networkInterface));
    }

    public void updateVectorClock(Map<UUID, Integer> vectorClockReceived) {
        for (UUID uuid : vectorClock.keySet()) {
            if (uuid != myself.getIdentifier()) {
                vectorClock.replace(uuid, Math.max(vectorClock.get(uuid), vectorClockReceived.getOrDefault(uuid, 0)));
            }
        }
    }

}
