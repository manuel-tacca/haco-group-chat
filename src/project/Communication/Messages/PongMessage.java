package project.Communication.Messages;

import project.Model.Peer;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;

/**
 * This class represents the message that is sent by a peer connected to the LAN to answer a {@link PingMessage}. Such
 * answer allows the peer who sent the {@link PingMessage} to add data about the other peers connected to the LAN.
 */
public class PongMessage extends Message implements Serializable {

    private final Peer peer;

    /**
     * Builds an instance of {@link PongMessage}.
     *
     * @param destinationAddress The destination address of the message.
     * @param destinationPort The destination port of the message.
     * @param peer The data about the sender.
     */
    public PongMessage(Map<UUID, Integer> vectorClock, InetAddress destinationAddress, int destinationPort, Peer peer) {
        super(MessageType.PONG, vectorClock, peer.getIdentifier(), destinationAddress, destinationPort);
        this.peer = peer;
    }

    /**
     * Returns the peer data of the user who sent a PONG.
     *
     * @return The peer data of the user who sent a PONG.
     */
    public Peer getPeer() {
        return peer;
    }

}
