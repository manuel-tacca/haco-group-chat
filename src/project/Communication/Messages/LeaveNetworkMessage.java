package project.Communication.Messages;

import project.Model.Peer;
import java.net.InetAddress;
import java.util.UUID;

/**
 * This class represents the message that is sent by a peer when they are (gracefully) closing the application.
 * In this way, the peers connected to the network can safely remove such peer from the list of known peers. Hence,
 * its IP address can potentially be used by a new peer.
 */
public class LeaveNetworkMessage extends Message {

    private final Peer peer;
    private final UUID ackID;

    /**
     * Builds an instance of {@link DeleteRoomMessage}.
     *
     * @param destinationAddress The destination address of the message.
     * @param destinationPort The destination port of the message.
     * @param peer The peer that is leaving the network.
     */
    public LeaveNetworkMessage(InetAddress destinationAddress, int destinationPort, Peer peer, UUID ackID) {
        super(MessageType.LEAVE_NETWORK, null, destinationAddress, destinationPort);
        this.peer = peer;
        this.ackID = ackID;
    }

    /**
     * Returns the peer that is leaving the network.
     *
     * @return The peer that is leaving the network.
     */
    public Peer getPeer() {
        return peer;
    }

    public UUID getAckID() { return ackID; }
}
