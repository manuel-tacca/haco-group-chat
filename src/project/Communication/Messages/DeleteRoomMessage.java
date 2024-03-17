package project.Communication.Messages;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;

public class DeleteRoomMessage extends Message implements Serializable {
    private final UUID roomUUID;

    public DeleteRoomMessage(Map<UUID, Integer> vectorClock, InetAddress destinationAddress, int destinationPort, UUID roomUUID) {
        super(MessageType.DELETE_ROOM, vectorClock, destinationAddress, destinationPort);
        this.roomUUID = roomUUID;
    }

    public UUID getRoomUUID() { return roomUUID; }
}
