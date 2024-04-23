package project.Communication.Messages;

import project.Model.RoomText;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;

/**
 * This class represents the message that is exchanged among peers in a room when chatting.
 */
public class RoomTextMessage extends Message implements Serializable {

    private final RoomText roomText;
    private final Map<UUID, Integer> vectorClock;

    /**
     * Builds an instance of {@link RoomTextMessage}.
     *
     * @param vectorClock The vector clock attached to this message.
     * @param destinationAddress The destination address of the message.
     * @param destinationPort The destination port of the message.
     * @param roomText The shared textual message.
     */
    public RoomTextMessage(Map<UUID, Integer> vectorClock, UUID senderUUID, InetAddress destinationAddress, int destinationPort, RoomText roomText) {
        super(MessageType.ROOM_TEXT, senderUUID, destinationAddress, destinationPort);
        this.vectorClock = vectorClock;
        this.roomText = roomText;
    }

    public RoomText getRoomText() {
        return roomText;
    }

    /**
     * Returns the vector clock attached to the message.
     *
     * @return The vector clock attached to the message.
     */
    public Map<UUID, Integer> getVectorClock() { return vectorClock; }

}
