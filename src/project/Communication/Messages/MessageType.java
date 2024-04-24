package project.Communication.Messages;

import java.io.Serializable;

/**
 * This enumeration specifies all the types of messages that can be exchanged between peers. It is meant to gracefully
 * replace an excessive use of the "instanceof" keyword.
 */
public enum MessageType implements Serializable {
    PING,
    PONG,
    ROOM_MEMBERSHIP,
    ROOM_TEXT,
    DELETE_ROOM,
    LEAVE_NETWORK,
    ACK_UNI,
    ACK_MULTI
}
