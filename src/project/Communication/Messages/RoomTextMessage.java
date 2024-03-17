package project.Communication.Messages;

import project.Model.RoomText;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;

public class RoomTextMessage extends Message implements Serializable {

    private final RoomText roomText;

    public RoomTextMessage(Map<UUID, Integer> vectorClock, InetAddress destinationAddress, int destinationPort, RoomText roomText) {
        super(MessageType.ROOM_TEXT, vectorClock, destinationAddress, destinationPort);
        this.roomText = roomText;
    }

    public RoomText getRoomText() {
        return roomText;
    }

}
