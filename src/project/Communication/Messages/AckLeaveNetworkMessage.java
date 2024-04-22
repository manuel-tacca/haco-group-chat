package project.Communication.Messages;

import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;

public class AckLeaveNetworkMessage extends Message{
    private final UUID ackID;

    public AckLeaveNetworkMessage(MessageType type, Map<UUID, Integer> vectorClock, InetAddress destinationAddress,
            int destinationPort, UUID ackID) {
        super(type, vectorClock, destinationAddress, destinationPort);
        this.ackID = ackID;
    }

    public UUID getAckID() { return ackID; }
}
