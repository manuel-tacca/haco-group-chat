import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Client {
    private static final int PORT = 9999;

    private String username;
    private InetAddress ipAddress;
    private InetAddress broadcastAddress;
    private Listener listener;
    private List<Peer> peers;
    private DatagramSocket socket;

    public Client() {
        peers = new ArrayList<>();
        try {
            this.socket = new DatagramSocket();
            this.socket.setBroadcast(true);
            //this.ipAddress = InetAddress.getLocalHost();
            //this.broadcastAddress = extractBroadcastAddress(ipAddress);
            this.ipAddress = InetAddress.getByName("192.168.1.149");
            this.broadcastAddress = InetAddress.getByName("192.168.1.255");
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

    public List<Peer> getPeers() {
        return peers;
    }

    public void addPeer(Peer p){
        peers.add(p);
    }

    public void printPeers(){
        for (Peer peer : peers) {
            System.out.println("Username: " + peer.getUsername());
            System.out.println("IP Address: " + peer.getIpAddress().getHostAddress());
            System.out.println("Port: " + peer.getPort());
            System.out.println();
        }
    }

    public void sendPing() {       
        try {
            byte[] pingMessage = "PING".getBytes();
            DatagramPacket ping = new DatagramPacket(pingMessage, pingMessage.length, broadcastAddress, PORT);
            socket.send(ping);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    /*public static InetAddress extractBroadcastAddress(InetAddress ipAddress) throws UnknownHostException {
        byte[] addr = ipAddress.getAddress();
        byte[] mask = ipAddress instanceof java.net.Inet4Address ? new byte[] {(byte)255, (byte)255, (byte)255, (byte)0} : new byte[] {(byte)255, (byte)255, (byte)255, (byte)255, (byte)0, (byte)0, (byte)0, (byte)0};
        byte[] broadcast = new byte[addr.length];
        
        for (int i = 0; i < addr.length; i++) {
            broadcast[i] = (byte) (addr[i] | ~mask[i]);
        }
        
        return InetAddress.getByAddress(broadcast);
    }*/
}
