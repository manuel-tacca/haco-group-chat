package project.Communication.Messages;

import project.Model.Room;

import java.io.Serializable;
import java.net.InetAddress;

public class RoomMembershipMessage extends Message implements Serializable {

    private final Room room;

    public RoomMembershipMessage(InetAddress destinationAddress, int destinationPort, Room room) {
        super(MessageType.ROOM_MEMBERSHIP, destinationAddress, destinationPort);
        this.room = room;
    }

    public Room getRoom() {
        return room;
    }

}
