import java.net.InetAddress;

public class Peer {
    private String username;
    private int port;
    private InetAddress ipAddress;

    public Peer(String username, int port, InetAddress ipAddress) {
        this.username = username;
        this.port = port;
        this.ipAddress = ipAddress;
    }

    public String getUsername(){
        return username;
    }

    public int getPort(){
        return port;
    }

    public InetAddress getIpAddress(){
        return ipAddress;
    }
}
