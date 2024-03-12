package project.Model;

import java.net.InetAddress;
import java.util.UUID;

public class Peer {

    private UUID identifier;
    private String username;
    private int port;
    private InetAddress ipAddress;

    public Peer(String username, InetAddress ipAddress, int port) {
        this.identifier = UUID.randomUUID();
        this.username = username;
        this.port = port;
        this.ipAddress = ipAddress;
    }

    public Peer(String uuid, String username, InetAddress ipAddress, int port) {
        this.identifier = UUID.fromString(uuid);
        this.username = username;
        this.port = port;
        this.ipAddress = ipAddress;
    }

    public Peer(String uuid, String username) {
        this.identifier = UUID.fromString(uuid);
        this.username = username;
    }

    public UUID getIdentifier(){
        return identifier;
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
