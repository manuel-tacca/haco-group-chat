package project;

import project.Exceptions.EmptyRoomException;
import project.Exceptions.InvalidParameterException;
import project.Exceptions.PeerAlreadyPresentException;
import project.Rooms.CreatedRoom;
import project.Rooms.Room;
import project.Runnables.Listener;
import project.Utils.CLIUtils;
import project.Messages.MessageBuilder;
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
            byte[] pingMessage = MessageBuilder.ping(myself.getIdentifier() + "," + myself.getUsername());
            SocketUtils.sendPacket(socket, pingMessage, broadcastAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createRoomStart() throws IOException {

        out.println("What name should the room have?");
        String roomName = inScanner.nextLine();
        CreatedRoom room = new CreatedRoom(roomName);

        int choice;
        do{
            out.println("Please, enter the number of the peer you want to add to the room [press 0 when you are done]:");
            CLIUtils.printPeers(peers);
            choice = inScanner.nextInt();
            try {
                room.addPeer(peers.get(choice - 1));
            }
            catch(IndexOutOfBoundsException e1){
                if (choice != 0) {
                    out.println("There's no peer with such a number.");
                }
            }
            catch(PeerAlreadyPresentException e2){
                out.println("Such peer is already present in the room.");
            }
            catch(Exception e){
                out.println("The given input is incorrect. Please try again.");
            }
        }while(choice != 0);

        if (room.getOtherRoomMembers().isEmpty()) {
            try {
                throw new EmptyRoomException(null);
            } catch (EmptyRoomException e) {
                out.println("You tried to create an empty room. Please try again");
                return;
            }
        }

        this.createdRooms.add(room);
        out.println("You have created the " + room.getName() + " room! Here are the members:");
            CLIUtils.printPeers(room.getOtherRoomMembers());
        
        for (Peer p : room.getOtherRoomMembers()) {

            byte[] roomMemberStartMessage = MessageBuilder.roomMemberStart(room.getIdentifier().toString(), room.getName(), myself);
            SocketUtils.sendPacket(socket, roomMemberStartMessage, p.getIpAddress());

            int count = room.getOtherRoomMembers().size() - 1;
            for (Peer p1 : room.getOtherRoomMembers()) {
                if(count == 1){
                    break;
                }
                if (!p1.getIdentifier().toString().equals(p.getIdentifier().toString())) {
                    byte[] roomMemberMessage = MessageBuilder.roomMember(room.getIdentifier().toString(), p1);
                    SocketUtils.sendPacket(socket, roomMemberMessage, p.getIpAddress());
                    count--;
                }
            }

            /*byte[] roomMemberStopMessage = MessageBuilder.roomMemberStop(room.getIdentifier().toString(), room.getOtherRoomMembers().get(room.getOtherRoomMembers().size() - 1));
            SocketUtils.sendPacket(socket, roomMemberStartMessage, p.getIpAddress(), SocketUtils.PORT_NUMBER);*/

        }
    }

    public void createRoomMembership(Peer creator, String roomID, String roomName){
        out.println("You have been inserted in a new room by "+creator.getUsername()+"! The ID of the room is: "+roomID);
        Room room = new Room(roomID, roomName);
        try {
            room.addPeer(creator);
        }
        catch(PeerAlreadyPresentException e){
            throw new RuntimeException(e.getMessage());
        }
        participatingRooms.add(room);
    }

    public void handleAck(String roomID, String userID) throws InvalidParameterException{
        Optional<CreatedRoom> room = createdRooms.stream().filter(x -> x.getIdentifier().toString().equals(roomID)).findFirst();
        if (room.isPresent()){
            room.get().confirmAck(UUID.fromString(userID));
        }
        else{
            throw new InvalidParameterException("There is no room with such UUID: " + roomID);
        }
    }

    public void addRoomMember(String roomID, Peer newPeer) throws Exception{
        Optional<CreatedRoom> room = createdRooms.stream().filter(x -> x.getIdentifier().toString().equals(roomID)).findFirst();
        if (room.isPresent()){
            room.get().addPeer(newPeer);
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
}
