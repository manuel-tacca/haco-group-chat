import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client {
    private static final int PORT = 9999;

    private String username;
    private InetAddress ipAddress;
    private InetAddress broadcastAddress;
    private Listener listener;
    private List<Peer> peers;
    private DatagramSocket socket;
    private List<Room> createdRooms;
    private List<Room> participatingRooms;

    public Client() {
        peers = new ArrayList<>();
        try {
            this.socket = new DatagramSocket();
            this.socket.setBroadcast(true);
            this.ipAddress = InetAddress.getLocalHost();
            this.broadcastAddress = extractBroadcastAddress(ipAddress);
            //this.ipAddress = InetAddress.getLocalHost();
            //this.broadcastAddress = InetAddress.getByName("192.168.1.255");
            this.listener = new Listener(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Listener getListener(){
        return listener;
    }

    public InetAddress getIpAddress(){
        return ipAddress;
    }

    public InetAddress getBroadcastAddress(){
        return broadcastAddress;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public String getUsername(){
        return username;
    }

    public void setUsername(String username){
        this.username = username;
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

    public void addPeer(Peer p){
        Boolean inList = false;
        if (!p.getIpAddress().equals(InetAddress.getLoopbackAddress())) {    
            for (Peer pInList : this.peers) {
                if (!p.getIpAddress().equals(pInList.getIpAddress())) {
                    inList=true;
                    break;
                }
            }
            if(!inList)
                peers.add(p);
        }
    }

    public void printPeers(){
        int i=1;
        for (Peer peer : peers) {
            System.out.println("Peer"+i+":");
            System.out.println("    Username: " + peer.getUsername());
            System.out.println("    IP Address: " + peer.getIpAddress().getHostAddress());
            System.out.println("    Port: " + peer.getPort());
            System.out.println();
            i++;
        }
    }

    public void sendPing() {       
        try {
            String pingString = "PING;"+this.username;
            byte[] pingMessage = pingString.getBytes();
            DatagramPacket ping = new DatagramPacket(pingMessage, pingMessage.length, broadcastAddress, PORT);
            socket.send(ping);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createRoomStart() throws IOException {
        System.out.println("Very good! you choose to create a new room!");
        Scanner inScanner = new Scanner(System.in);
        System.out.println("What name should the room have?");
        String roomName = inScanner.nextLine();
        Room room = new Room(roomName, this.socket.getInetAddress(), new Peer(username, PORT, InetAddress.getLoopbackAddress())); //The messages i will send will be received by all members of the room, myself included.
        int choice = 0;
        do{
            System.out.println("Please, select from the list of peers the participant to this room [press 0 when you are done]:");
            printPeers();
            room.addPeer(peers.get(choice-1));
        }while(choice != 0); // (meccanismo per capire quali peer sono connessi e quali no.)
        System.out.println("You have created a new room! Here are the members:");
        room.printPeers();
        this.createdRooms.add(room);
        // Locally the room is registered, let's now tell the participating peers that they were added to this room
        String roomMembershipStartString, roomMembershipIPString, roomMembershipStopString;
        byte[] roomMembershipStartMessage, roomMembershipIPMessage, roomMembershipStopMessage;
        DatagramPacket roomMembershipStartPacket, roomMemberhipIPPacket, roomMembershipStopPacket;
        
        for (Peer p : room.getRoomMembers()) {
            if (!p.getIpAddress().equals(InetAddress.getLoopbackAddress())) {
                roomMembershipStartString = "ROOM_MEMBER_START;"+room.getIdentifier();
                roomMembershipStartMessage = roomMembershipStartString.getBytes();
                roomMembershipStartPacket = new DatagramPacket(roomMembershipStartMessage, roomMembershipStartMessage.length, p.getIpAddress(), PORT);
                socket.send(roomMembershipStartPacket);
                //invia lista di ip associati a membri della room + ";ROOM_MEMBER_STOP"
                
                /*for (Peer p1 : room.getRoomMembers()) {
                    if (!p1.getIpAddress().equals(p.getIpAddress())) {
                        roomMembershipIPString = p1.getIpAddress().toString();
                        roomMembershipIPMessage = roomMembershipIPString.getBytes();
                        roomMemberhipIPPacket = new DatagramPacket(roomMembershipIPMessage, roomMembershipIPMessage.length, p.getIpAddress(), PORT);
                        socket.send(roomMemberhipIPPacket);
                    }
                }
                roomMembershipStopString = "ROOM_MEMBER_STOP;";
                roomMembershipStopMessage = roomMembershipStopString.getBytes();
                roomMembershipStopPacket = new DatagramPacket(roomMembershipStopMessage, roomMembershipStopMessage.length, p.getIpAddress(), PORT);
                socket.send(roomMembershipStopPacket);*/

                //OPPURE discovery tra membri che hanno ricevuto il pacchetto ROOM_MEMBER_START;ROOMID
                
            }
        }
    }

    public void sendParticipationQueryMessage(String roomID) {
        // I want to send in broadcast a message asking who is participating to this room
        try {
            String participationQueryString = "MEMBER?;"+roomID;
            byte[] participationQueryMessage = participationQueryString.getBytes();
            DatagramPacket ping = new DatagramPacket(participationQueryMessage, participationQueryMessage.length, broadcastAddress, PORT);
            socket.send(ping);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createRoomMembership(InetAddress creatorAddress, String roomID){
        System.out.println("You have been inserted in a new room by "+creatorAddress.getHostAddress()+"! The ID of the room is: "+roomID);
        Room room = new Room(roomID, creatorAddress, new Peer(username, PORT, InetAddress.getLoopbackAddress()));
        participatingRooms.add(room);

    }

    public void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
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
