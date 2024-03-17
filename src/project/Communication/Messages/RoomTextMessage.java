package project.Communication.Messages;

import project.Model.RoomText;

import java.io.Serializable;
import java.net.InetAddress;

public class RoomTextMessage extends Message implements Serializable {

    private final RoomText roomText;

    public RoomTextMessage(InetAddress destinationAddress, int destinationPort, RoomText roomText) {
        super(MessageType.ROOM_TEXT, destinationAddress, destinationPort);
        this.roomText = roomText;
    }

    public RoomText getRoomText() {
        return roomText;
    }

}
