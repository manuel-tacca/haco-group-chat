package project;

import project.CLI.CLI;
import project.Exceptions.EmptyRoomException;
import project.Exceptions.InvalidParameterException;
import project.Exceptions.PeerAlreadyPresentException;
import project.Rooms.CreatedRoom;
import project.Rooms.Room;
import project.Runnables.Listener;
import project.Messages.MessageBuilder;
import project.Messages.Message;
import project.Utils.SocketUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class Client {

    private Peer myself;
    private InetAddress broadcastAddress;
    private Listener listener;
    private DatagramSocket socket;
    private List<Peer> peers;
    private List<CreatedRoom> createdRooms;
    private List<Room> participatingRooms;
    private Scanner inScanner;
    private int sequenceNumber;
    private Map<Integer, Message> pendingAcks;

    private final PrintStream out = System.out;

    public Client(String username) {
        peers = new ArrayList<>();
        createdRooms = new ArrayList<>();
        participatingRooms = new ArrayList<>();
        try {
            this.socket = new DatagramSocket();
            this.socket.setBroadcast(true);
            String ip;
            try(final DatagramSocket socket = new DatagramSocket()){
                socket.connect(InetAddress.getByName("8.8.8.8"), SocketUtils.PORT_NUMBER);
                ip = socket.getLocalAddress().getHostAddress();
            }
            this.myself = new Peer(username, InetAddress.getByName(ip), SocketUtils.PORT_NUMBER);
            this.broadcastAddress = extractBroadcastAddress(myself.getIpAddress());
            this.listener = new Listener(this);
            inScanner = new Scanner(System.in);
            sequenceNumber = 0;
            pendingAcks = new HashMap<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Listener getListener(){
        return listener;
    }

    public InetAddress getBroadcastAddress(){
        return broadcastAddress;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public Peer getPeerData(){
        return myself;
    }

    public List<CreatedRoom> getCreatedRooms() {
        return createdRooms;
    }

    public List<Room> getParticipatingRooms() {
        return participatingRooms;
    }

    public List<Peer> getPeers() {
        return peers;
    }

    public void addPeer(Peer p) throws PeerAlreadyPresentException{
        for (Peer peer : this.peers) {
            if (p.getIdentifier().toString().equals(peer.getIdentifier().toString())) {
                throw new PeerAlreadyPresentException("There's already a peer with such an ID.");
            }
        }
        peers.add(p);
    }

    public void sendPing() {       
        try {
            Message pingMessage = MessageBuilder.ping(myself.getIdentifier().toString(), myself.getUsername(), broadcastAddress);
            SocketUtils.sendPacket(socket, pingMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createRoom(String roomName, String[] peerIds) throws Exception {

        CreatedRoom room = new CreatedRoom(roomName);
        for(String peerId: peerIds){
            int id = Integer.parseInt(peerId);
            room.addPeer(peers.get(id - 1));
        }

        if (room.getOtherRoomMembers().isEmpty()) {
            throw new EmptyRoomException(null);
        }

        this.createdRooms.add(room);
        
        for (Peer p : room.getOtherRoomMembers()) {

            Message roomMemberStartMessage = MessageBuilder.roomMemberStart(room.getIdentifier().toString(),
                    room.getName(), myself, room.getOtherRoomMembers().size(), p.getIpAddress(), getAndIncrementSequenceNumber());
            SocketUtils.sendPacket(socket, roomMemberStartMessage);

            for (Peer p1 : room.getOtherRoomMembers()) {
                if (!p1.getIdentifier().toString().equals(p.getIdentifier().toString())) {
                    Message roomMemberMessage = MessageBuilder.roomMember(room.getIdentifier().toString(), p1, p.getIpAddress());
                    SocketUtils.sendPacket(socket, roomMemberMessage);
                }
            }

            /*byte[] roomMemberStopMessage = MessageBuilder.roomMemberStop(room.getIdentifier().toString(), room.getOtherRoomMembers().get(room.getOtherRoomMembers().size() - 1));
            SocketUtils.sendPacket(socket, roomMemberStartMessage, p.getIpAddress(), SocketUtils.PORT_NUMBER);*/

        }
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

    public void putInPending(int sequenceNumber, Message message){
        pendingAcks.put(sequenceNumber, message);
    }

    public void acknowledge(int sequenceNumber){
        pendingAcks.remove(sequenceNumber);
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

    public void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        if (listener.getSocket() != null && !listener.getSocket().isClosed()) {
            listener.getSocket().close();
        }
        inScanner.close();
    }

    public static InetAddress extractBroadcastAddress(InetAddress ipAddress) throws UnknownHostException {
        byte[] addr = ipAddress.getAddress();
        byte[] mask = ipAddress instanceof java.net.Inet4Address ? new byte[] {(byte)255, (byte)255, (byte)255, (byte)0} : new byte[] {(byte)255, (byte)255, (byte)255, (byte)255, (byte)0, (byte)0, (byte)0, (byte)0};
        byte[] broadcast = new byte[addr.length];
        
        for (int i = 0; i < addr.length; i++) {
            broadcast[i] = (byte) (addr[i] | ~mask[i]);
        }
        
        return InetAddress.getByAddress(broadcast);
    }

    public int getAndIncrementSequenceNumber(){
        int result = sequenceNumber;
        sequenceNumber++;
        return result;
    }
}
