package project.Communication.Messages;

import project.Model.Peer;
import java.net.InetAddress;

/**
 * This class represents the message that is sent by a peer when they are (gracefully) closing the application.
 * In this way, the peers connected to the network can safely remove such peer from the list of known peers. Hence,
 * its IP address can potentially be used by a new peer.
 */
public class LeaveNetworkMessage extends Message {

    private final Peer peer;

    /**
     * Builds an instance of {@link DeleteRoomMessage}.
     *
     * @param destinationAddress The destination address of the message.
     * @param destinationPort The destination port of the message.
     * @param peer The peer that is leaving the network.
     */
    public LeaveNetworkMessage(InetAddress destinationAddress, int destinationPort, Peer peer) {
        super(MessageType.LEAVE_NETWORK, null, destinationAddress, destinationPort);
        this.peer = peer;
    }

    /**
     * Returns the peer that is leaving the network.
     *
     * @return The peer that is leaving the network.
     */
    public Peer getPeer() {
        return peer;
    }
}
