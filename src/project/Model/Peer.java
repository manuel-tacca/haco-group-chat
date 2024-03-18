package project.Model;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.UUID;

/**
 * This class represents a peer in the network. Each peer is identified by a unique UUID. Since information about peers
 * needs to be exchanged, this class is serializable.
 */
public class Peer implements Serializable {

    private final UUID identifier;
    private final String username;
    private final InetAddress ipAddress;

    /**
     * Builds a new instance of a peer. This constructor should be used only to build the Peer instance of the
     * user itself, since no UUID is requested (it is randomly generated).
     *
     * @param username The username of the peer.
     * @param ipAddress The IP address of the peer.
     */
    public Peer(String username, InetAddress ipAddress){
        this.identifier = UUID.randomUUID();
        this.username = username;
        this.ipAddress = ipAddress;
    }

    // GETTERS

    /**
     * Returns the UUID of the peer.
     *
     * @return The UUID of the peer.
     */
    public UUID getIdentifier(){
        return identifier;
    }

    /**
     * Returns the username of the peer.
     *
     * @return The username of the peer.
     */
    public String getUsername(){
        return username;
    }

    /**
     * Returns the IP address of the peer.
     *
     * @return The IP address of the peer.
     */
    public InetAddress getIpAddress() {
        return ipAddress;
    }
}
