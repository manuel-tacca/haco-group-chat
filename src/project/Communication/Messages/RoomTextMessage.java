package project.Communication.Messages;

import project.Model.RoomText;

import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;

/**
 * This class represents the message that is exchanged among peers in a room when chatting.
 */
public class RoomTextMessage extends Message {

    private final RoomText roomText;
    private final UUID ackID;

    /**
     * Builds an instance of {@link RoomTextMessage}.
     *
     * @param vectorClock The vector clock attached to this message.
     * @param destinationAddress The destination address of the message.
     * @param destinationPort The destination port of the message.
     * @param roomText The shared textual message.
     */
    public RoomTextMessage(Map<UUID, Integer> vectorClock, InetAddress destinationAddress, int destinationPort, RoomText roomText, UUID ackID) {
        super(MessageType.ROOM_TEXT, vectorClock, destinationAddress, destinationPort);
        this.roomText = roomText;
        this.ackID = ackID;
    }

    public RoomText getRoomText() {
        return roomText;
    }


    public UUID getAckID() { return ackID; }
}
