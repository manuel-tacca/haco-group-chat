package project.Communication.Messages;

import project.Model.Peer;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.UUID;

/**
 * This class represents the message that is sent by a peer to notify other peers about its presence in the LAN and to
 * discover other peers currently connected in the LAN.
 */
public class PingMessage extends Message implements Serializable {

    private final Peer peer;

    /**
     * Builds an instance of {@link PingMessage}.
     *
     * @param destinationAddress The destination address of the message.
     * @param destinationPort The destination port of the message.
     * @param peer The newly connected user.
     */
    public PingMessage(InetAddress destinationAddress, int destinationPort, Peer peer, UUID ackID) {
        super(MessageType.PING, null, destinationAddress, destinationPort, ackID);
        this.peer = peer;
    }

    /**
     * Returns the peer data of the user who sent a PING.
     *
     * @return The peer data of the user who sent a PING.
     */
    public Peer getPeer() {
        return peer;
    }

}
