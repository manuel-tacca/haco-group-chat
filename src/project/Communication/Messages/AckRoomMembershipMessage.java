package project.Communication.Messages;

import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;

public class AckRoomMembershipMessage extends Message{
    private final UUID ackID;
    private final InetAddress sourceAddress;

    
    public AckRoomMembershipMessage(MessageType type, Map<UUID, Integer> vectorClock, InetAddress destinationAddress, int destinationPort, UUID ackID, InetAddress sourceAddress) {
        super(type, vectorClock, destinationAddress, destinationPort);
        this.ackID = ackID;
        this.sourceAddress = sourceAddress;
    }
    
    public UUID getAckID() {
        return ackID;
    }

    public InetAddress getSourceAddress() {
        return sourceAddress;
    }
}
