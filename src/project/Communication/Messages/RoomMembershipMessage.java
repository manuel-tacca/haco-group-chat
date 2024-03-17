package project.Communication.Messages;

import project.Model.Room;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;

public class RoomMembershipMessage extends Message implements Serializable {

    private final Room room;

    public RoomMembershipMessage(Map<UUID, Integer> vectorClock, InetAddress destinationAddress, int destinationPort, Room room) {
        super(MessageType.ROOM_MEMBERSHIP, vectorClock, destinationAddress, destinationPort);
        this.room = room;
    }

    public Room getRoom() {
        return room;
    }

}
