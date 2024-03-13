package project;

import project.CLI.CLI;
import project.Communication.Listeners.Listener;
import project.Communication.Listeners.MulticastListener;
import project.Communication.PacketHandlers.MulticastPacketHandler;
import project.Communication.PacketHandlers.PacketHandler;
import project.Exceptions.*;
import project.Model.Peer;
import project.Model.CreatedRoom;
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
import java.util.regex.Pattern;

import static java.lang.System.out;

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
    private Room stubRoom;
    private Map<String, StringBuilder> roomMessages;

    public Client(String username) throws Exception {
        peers = new ArrayList<>();
        createdRooms = new ArrayList<>();
        participatingRooms = new ArrayList<>();
        multicastPacketHandlers = new ArrayList<>();
        inScanner = new Scanner(System.in);
        String ip;

        // connects to the network
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), Sender.PORT_NUMBER);
            ip = socket.getLocalAddress().getHostAddress();
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }
        CLI.printDebug(ip);

        this.myself = new Peer(username);
        this.packetHandler = new PacketHandler(this);
        this.sender = new Sender();
        sender.sendPendingPacketsAtFixedRate(1);
        this.broadcastAddress = extractBroadcastAddress(InetAddress.getByName(ip));
        stubRoom = new Room(UUID.randomUUID().toString(), "stub", 0);
        currentlyDisplayedRoom = stubRoom;
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

    public MulticastListener getMulticastListeners(UUID roomUUID){
        List<MulticastListener> multicastListeners = new ArrayList<>();
        for(MulticastPacketHandler multicastPacketHandler: multicastPacketHandlers){
            multicastListeners.add(multicastPacketHandler.getMulticastListener());
        }
        return multicastListeners.stream().filter(x -> x.getRoomIdentifier().equals(roomUUID)).findFirst().get();
    }

    public String getMessagesForRoom(String roomID) {
        return roomMessages.getOrDefault(roomID, new StringBuilder()).toString();
    }

    public void addPeer(Peer p) throws PeerAlreadyPresentException{
        for (Peer peer : this.peers) {
            if (p.getIdentifier().toString().equals(peer.getIdentifier().toString())) {
                throw new PeerAlreadyPresentException("There's already a peer with such an ID.");
            }
        }
        peers.add(p);
    }

    public void discoverNewPeers() throws IOException{
        Message pingMessage = MessageBuilder.ping(myself.getIdentifier().toString(), myself.getUsername(), broadcastAddress);
        sendPacket(pingMessage);
    }

    public void createRoom(String roomName, String[] peerIds) throws Exception {

        // 2. crea una nuova CreatedRoom. Room è stata modificata mantenendo address e port del multicast come attributi
        CreatedRoom room = new CreatedRoom(roomName);

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
            /*Message roomMembershipMessage = MessageBuilder.roomMembership(getProcessID(), room.getIdentifier().toString(), room.getMulticastAddress().toString(), room.getMulticastPort(),
                                                                            room.getName(), myself, room.getOtherRoomMembers(), p.getIdentifier());
            sendPacket(roomMembershipMessage);/*

        /*    Message roomMemberStartMessage = MessageBuilder.roomMemberStart(getProcessID(), room.getIdentifier().toString(),
                    room.getName(), myself, room.getOtherRoomMembers().size(), p.getIpAddress(), p.getIdentifier());
            sendPacket(roomMemberStartMessage);

            for (Peer p1 : room.getOtherRoomMembers()) {
                if (!p1.getIdentifier().toString().equals(p.getIdentifier().toString())) {
                    Message roomMemberMessage = MessageBuilder.roomMember(getProcessID(), room.getIdentifier().toString(), p1, p.getIpAddress(), p.getIdentifier());
                    sendPacket(roomMemberMessage);
                }
            }
        */
        }

        // 4. instanzia un nuovo thread che ascolti sul multicastAddress deciso dall'utente. La nuova classe roomHandler servirà a gestire i pacchetti ricevuti relativi ad ogni room
        //    + tengo una lista di roomHandlers in client. Prosegui su packetHandler.
        multicastPacketHandlers.add(new MulticastPacketHandler(this, room));
    }

    public void createRoomMembership(Peer creator, String roomID, String roomName, int membersNumber){
        Room room = new Room(roomID, roomName, membersNumber);
        CLI.appendNotification("You have been inserted in a new room by "+creator.getUsername()+"! The ID of the room is: "+roomID);
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

    public void addRoomMember(String roomID, Peer newPeer) throws Exception{
        Optional<Room> room = participatingRooms.stream().filter(x -> x.getIdentifier().toString().equals(roomID)).findFirst();
        if (room.isPresent()){
            room.get().addPeer(newPeer);
            if (room.get().getMembersNumber() == room.get().getOtherRoomMembers().size()) {
                String creator = room.get().getOtherRoomMembers().get(0).getUsername();
                CLI.appendNotification("You have been inserted in a new room by "+creator+"! The ID of the room is: "+roomID);
            }
        }
        else{
            throw new InvalidParameterException("There is no room with such UUID: " + roomID);
        }
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
        Message request = MessageBuilder.memberInfoRequest(myself.getIdentifier().toString(), missingPeerID, roomID, destinationAddress, creatorUUID);
        sendPacket(request);
    }

    public void close() {
        sender.close();
        packetHandler.shutdown();
        multicastPacketHandlers.forEach(MulticastPacketHandler::shutdown);
        inScanner.close();
    }

    private InetAddress extractBroadcastAddress(InetAddress ipAddress) throws UnknownHostException {
        byte[] addr = ipAddress.getAddress();
        byte[] mask = ipAddress instanceof java.net.Inet4Address ? new byte[] {(byte)255, (byte)255, (byte)255, (byte)0} : new byte[] {(byte)255, (byte)255, (byte)255, (byte)255, (byte)0, (byte)0, (byte)0, (byte)0};
        byte[] broadcast = new byte[addr.length];
        
        for (int i = 0; i < addr.length; i++) {
            broadcast[i] = (byte) (addr[i] | ~mask[i]);
        }
        
        return InetAddress.getByAddress(broadcast);
    }

    public String getProcessID(){
        return myself.getIdentifier().toString();
    }

    public void sendPacket(Message message) throws IOException {
        sender.sendPacket(message);
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

    public Boolean checkCorrectIpFormat(String ipAddress) {
        String IP_ADDRESS_PATTERN =
                "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                        + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                        + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                        + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
        Pattern pattern = Pattern.compile(IP_ADDRESS_PATTERN);

        String[] octets = ipAddress.split("\\.");
        int firstOctet = Integer.parseInt(octets[0]);

        if(!(firstOctet >= 224 && firstOctet <= 239)) {
            CLI.printError("The provided address is not in the correct format!");
            return false;
        }
        return true;
    }

    public boolean checkCorrectPortFormat(String portS) {
        int port = Integer.parseInt(portS);
        return port >= 1024 && port <= 4151;
    }
}
