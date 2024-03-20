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

    /**
     * Builds an instance of {@link RoomTextMessage}.
     *
     * @param vectorClock The vector clock attached to this message.
     * @param destinationAddress The destination address of the message.
     * @param destinationPort The destination port of the message.
     * @param roomText The shared textual message.
     */
    public RoomTextMessage(Map<UUID, Integer> vectorClock, UUID senderUUID, InetAddress destinationAddress, int destinationPort, RoomText roomText) {
        super(MessageType.ROOM_TEXT, vectorClock, senderUUID, destinationAddress, destinationPort);
        this.roomText = roomText;
    }

    public RoomText getRoomText() {
        return roomText;
    }

}
