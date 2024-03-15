package project;

import project.CLI.CLI;
import project.Communication.Listeners.Listener;
import project.Communication.Listeners.MulticastListener;
import project.Communication.NetworkUtils;
import project.Communication.PacketHandlers.MulticastPacketHandler;
import project.Communication.PacketHandlers.PacketHandler;
import project.Exceptions.*;
import project.Model.Peer;
import project.Model.Room;
import project.Communication.Messages.MessageBuilder;
import project.Communication.Messages.Message;
import project.Communication.Sender;
import project.Model.RoomMessage;

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
    private final Set<Peer> peers;
    private final Set<Room> createdRooms;
    private final Set<Room> participatingRooms;
    private final Scanner inScanner;
    private Room currentlyDisplayedRoom;

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

        myself = new Peer(username, InetAddress.getByName(ip));
        packetHandler = new PacketHandler(this);
        sender = new Sender();
        sender.sendPendingPacketsAtFixedRate(1);
        broadcastAddress = NetworkUtils.getBroadcastAddress(myself.getIpAddress());
        currentlyDisplayedRoom = new Room("stub", null, null);
    }

    // GETTERS

    public Room getCurrentlyDisplayedRoom(){
        return currentlyDisplayedRoom;
    }

    public PacketHandler getPacketHandler(){
        return packetHandler;
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

    public Listener getListener(){
        return packetHandler.getListener();
    }

    // SPECIAL GETTERS

    public Room getRoom(String name) throws InvalidParameterException{
        Optional<Room> room = createdRooms.stream().filter(x -> x.getName().equals(name)).findFirst();
        if(room.isPresent()){
            return room.get();
        }
        else{
            throw new InvalidParameterException("There is no room with such a name: " + name);
        }
    }

    public Room getRoom(UUID uuid) throws InvalidParameterException {
        Optional<Room> room = createdRooms.stream().filter(x -> x.getIdentifier().equals(uuid)).findFirst();
        if(room.isPresent()){
            return room.get();
        }
        else{
            throw new InvalidParameterException("There is no room with such a UUID: " + uuid);
        }
    }

    public List<RoomMessage> getRoomMessages(String roomName) throws InvalidParameterException {
        return getRoom(roomName).getRoomMessages();
    }

    public List<RoomMessage> getRoomMessages(UUID roomUUID) throws InvalidParameterException {
        return getRoom(roomUUID).getRoomMessages();
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

    // SETTERS

    public void setCurrentlyDisplayedRoom(Room currentlyDisplayedRoom) {
        this.currentlyDisplayedRoom = currentlyDisplayedRoom;
    }

    // PUBLIC METHODS

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

    public void handleRoomMembership(UUID roomUUID, String roomName, InetAddress multicastAddress, Set<Peer> peers) throws Exception {

        Room room = new Room(roomUUID, roomName, peers, multicastAddress);
        participatingRooms.add(room);
        multicastPacketHandlers.add(new MulticastPacketHandler(this, room));

        // if some of the peers that are in the newly created room are not part of the known peers, add them
        for (Peer peer: peers){
            if (!this.peers.contains(peer)){
                addPeer(peer);
            }
        }
    }

    public void discoverNewPeers() throws IOException{
        Message pingMessage = MessageBuilder.ping(myself.getIdentifier(), myself.getUsername(), broadcastAddress, NetworkUtils.UNICAST_PORT_NUMBER);
        sender.sendPacket(pingMessage);
    }

    public void createRoom(String roomName, String[] peerIds) throws Exception {

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
        multicastPacketHandlers.add(new MulticastPacketHandler(this, room));

        // notifies the participating peers of the room creation
        for (Peer p : room.getRoomMembers()) {
            if(!p.getIdentifier().equals(myself.getIdentifier())) {
                Message roomMembershipMessage = MessageBuilder.roomMembership(myself.getIdentifier(), room.getIdentifier(),
                        room.getName(), room.getMulticastAddress(), room.getRoomMembers(), p.getIpAddress(), NetworkUtils.UNICAST_PORT_NUMBER);
                sender.sendPacket(roomMembershipMessage);
            }
        }
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
            for (Peer p : room.getRoomMembers()) {
                if(!p.getIdentifier().equals(myself.getIdentifier())) {
                    /*Message roomDeleteMessage = MessageBuilder.roomDelete(getProcessID(), room.getIdentifier().toString(), p.getIpAddress(), p.getIdentifier());
                    sendPacket(roomDeleteMessage);*/
                }
            }
            createdRooms.remove(room);
        }
    }

    public void deleteCreatedRoomMultipleChoice(Room roomSelected) throws IOException {
        for (Peer p : roomSelected.getRoomMembers()) {
            if(!p.getIdentifier().equals(myself.getIdentifier())) {
                    /*Message roomDeleteMessage = MessageBuilder.roomDelete(getProcessID(), room.getIdentifier().toString(), p.getIpAddress(), p.getIdentifier());
                    sendPacket(roomDeleteMessage);*/
            }
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

    public void sendRoomMessage(RoomMessage roomMessage){

    }

    public void findMissingPeer(InetAddress destinationAddress, String missingPeerID, String roomID) throws IOException {
        //TODO
    }

    public void close() {
        sender.close();
        packetHandler.shutdown();
        multicastPacketHandlers.forEach(MulticastPacketHandler::shutdown);
        inScanner.close();
    }

    public boolean existsRoom(String roomName) throws InvalidRoomNameException, SameRoomNameException {
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

    private void addPeer(Peer p) throws PeerAlreadyPresentException{
        for (Peer peer : this.peers) {
            if (p.getIdentifier().toString().equals(peer.getIdentifier().toString())) {
                throw new PeerAlreadyPresentException("There's already a peer with such an ID.");
            }
        }
        peers.add(p);
    }

}
