package project.Communication.Messages;

import java.io.Serializable;

public enum MessageType implements Serializable {
    PING,
    PONG,
    ROOM_MEMBERSHIP,
    ROOM_TEXT
}
