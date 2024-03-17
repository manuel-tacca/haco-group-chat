package project.Communication.Messages;

import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public abstract class Message implements Serializable {

    protected final MessageType type;
    protected final Map<UUID, Integer> vectorClock;
    protected final InetAddress destinationAddress;
    protected final int destinationPort;

    public Message(MessageType type, Map<UUID, Integer> vectorClock, InetAddress destinationAddress, int destinationPort) {
        this.type = type;
        this.vectorClock = vectorClock;
        this.destinationAddress = destinationAddress;
        this.destinationPort = destinationPort;
    }

    public MessageType getType() {
        return type;
    }

    public Map<UUID, Integer> getVectorClock() { return vectorClock; }

    public InetAddress getDestinationAddress() {
        return destinationAddress;
    }

    public int getDestinationPort() {
        return destinationPort;
    }
}
