package project.Communication.Messages;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;

/**
 * This class represents the message that is sent by the creator of a room when they want to notify other peers that
 * the room has been deleted.
 */
public class DeleteRoomMessage extends Message implements Serializable {
    private final UUID roomUUID;

    /**
     * Builds an instance of {@link DeleteRoomMessage}.
     *
     * @param vectorClock The vector clock attached to this message.
     * @param destinationAddress The destination address of the message.
     * @param destinationPort The destination port of the message.
     * @param roomUUID The UUID of the room to delete.
     */
    public DeleteRoomMessage(Map<UUID, Integer> vectorClock, InetAddress destinationAddress, int destinationPort, UUID roomUUID) {
        super(MessageType.DELETE_ROOM, vectorClock, destinationAddress, destinationPort);
        this.roomUUID = roomUUID;
    }

    /**
     * Returns the UUID of the room to delete.
     *
     * @return The UUID of the room to delete.
     */
    public UUID getRoomUUID() { return roomUUID; }
}
