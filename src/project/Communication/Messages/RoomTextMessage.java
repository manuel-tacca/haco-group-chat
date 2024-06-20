package project.Communication.Messages;

import project.Model.RoomText;
import project.Model.VectorClock;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.UUID;

/**
 * This class represents the message that is exchanged among peers in a room when chatting.
 */
public class RoomTextMessage extends Message implements Serializable {

    private final RoomText roomText;
    private final VectorClock vectorClock;

    /**
     * Builds an instance of {@link RoomTextMessage}.
     *
     * @param vectorClock The vector clock attached to this message.
     * @param senderUUID The UUID of the sender.
     * @param destinationAddress The destination address of the message.
     * @param destinationPort The destination port of the message.
     * @param roomText The shared textual message.
     * @param ackID The ackID of the message.
     */
    public RoomTextMessage(VectorClock vectorClock, UUID senderUUID, InetAddress destinationAddress, int destinationPort, RoomText roomText, UUID ackID) {
        super(MessageType.ROOM_TEXT, senderUUID, destinationAddress, destinationPort, ackID);
        this.vectorClock = vectorClock;
        this.roomText = roomText;
    }

    /**
     * Returns the room text attached to the message.
     *
     * @return The room text attached to the message.
     */
    public RoomText getRoomText() {
        return roomText;
    }

    /**
     * Returns the vector clock attached to the message.
     *
     * @return The vector clock attached to the message.
     */
    public VectorClock getVectorClock() { return vectorClock; }

}
