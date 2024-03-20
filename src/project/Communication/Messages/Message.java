package project.Communication.Messages;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;

/**
 * This abstract class represents a message that is serialized into a packet and sent into the LAN. Upon receipt,
 * it is deserialized and used by the peers. This class provides the base (i.e. the attributes that all the
 * messages should have) that must be extended by the messages that are actually sent into the network.
 */
public abstract class Message implements Serializable {

    protected final MessageType type;
    protected final Map<UUID, Integer> vectorClock;
    protected final UUID senderUUID;
    protected final InetAddress destinationAddress;
    protected final int destinationPort;

    /**
     * Sets the parameters that are common to every message.
     *
     * @param type               The type of the message.
     * @param vectorClock        The vector clock attached to the message.
     * @param senderUUID         The sender's UUID.
     * @param destinationAddress The destination address.
     * @param destinationPort    The destination port.
     */
    public Message(MessageType type, Map<UUID, Integer> vectorClock, UUID senderUUID, InetAddress destinationAddress, int destinationPort) {
        this.type = type;
        this.vectorClock = vectorClock;
        this.senderUUID = senderUUID;
        this.destinationAddress = destinationAddress;
        this.destinationPort = destinationPort;
    }

    /**
     * Returns the type of the message.
     *
     * @return The type of the message.
     */
    public MessageType getType() {
        return type;
    }

    /**
     * Returns the vector clock attached to the message.
     *
     * @return The vector clock attached to the message.
     */
    public Map<UUID, Integer> getVectorClock() { return vectorClock; }

    /**
     * Returns the destination address of the message.
     *
     * @return The destination address of the message.
     */
    public InetAddress getDestinationAddress() {
        return destinationAddress;
    }

    /**
     * Returns the destination port of the message.
     *
     * @return The destination port of the message.
     */
    public int getDestinationPort() {
        return destinationPort;
    }

    /**
     * Returns the sender's UUID.
     *
     * @return the sender's UUID
     */
    public UUID getSenderUUID() { return senderUUID; }
}
