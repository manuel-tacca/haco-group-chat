package project.Communication.Messages;

import java.net.InetAddress;
import java.util.UUID;

public class AckMessage extends Message {

    public AckMessage(MessageType type, UUID senderUUID, InetAddress destinationAddress, int destinationPort, UUID ackID) {
        super(type, senderUUID, destinationAddress, destinationPort, ackID);
    }
    
}
