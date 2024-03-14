package project.Model;

import java.net.InetAddress;
import java.util.UUID;

public class Peer {

    private final UUID identifier;
    private final String username;
    private final InetAddress ipAddress;

    public Peer(String username, InetAddress ipAddress){
        this.identifier = UUID.randomUUID();
        this.username = username;
        this.ipAddress = ipAddress;
    }

    public Peer(UUID uuid, String username, InetAddress ipAddress) {
        this.identifier = uuid;
        this.username = username;
        this.ipAddress = ipAddress;
    }

    public UUID getIdentifier(){
        return identifier;
    }

    public String getUsername(){
        return username;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }
}
