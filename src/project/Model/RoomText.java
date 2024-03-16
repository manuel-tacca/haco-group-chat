package project.Model;

import java.io.Serializable;
import java.util.UUID;

/**
 * This record represents a message that is exchanged in a room. Since peers need to exchange messages in rooms,
 * this class is serializable.
 */
public record RoomText(UUID roomUUID, Peer author, String content, boolean isWrittenByMe) implements Serializable {

}