package project.Communication.Messages;

import java.net.InetAddress;
import java.util.UUID;

/**
 * The AckMessage class represents an acknowledgment message sent after receiving a (non-Ack) message.
 * It extends the Message class and includes the necessary information for identifying and routing acknowledgment messages.
 */
public class AckMessage extends Message {

    /**
     * Builds an instance of {@link AckMessage}.
     *
     * @param type The type of the message.
     * @param senderUUID The UUID of the sender.
     * @param destinationAddress The destination IP address.
     * @param destinationPort The destination port number.
     * @param ackID The ackID of the message.
     */
    public AckMessage(MessageType type, UUID senderUUID, InetAddress destinationAddress, int destinationPort, UUID ackID) {
        super(type, senderUUID, destinationAddress, destinationPort, ackID);
    }
    
}
