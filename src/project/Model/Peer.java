package project.Model;

import java.net.InetAddress;
import java.util.UUID;

public class Peer {

    private UUID identifier;
    private String username;

    public Peer(String username){
        this.identifier = UUID.randomUUID();
    }

    public Peer(UUID uuid, String username) {
        this.identifier = uuid;
        this.username = username;
    }

    public UUID getIdentifier(){
        return identifier;
    }

    public String getUsername(){
        return username;
    }
}
