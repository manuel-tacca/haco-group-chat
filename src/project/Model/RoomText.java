package project.Model;

import java.io.Serializable;
import java.util.UUID;

/**
 * This record represents a text message that is exchanged in a room. Since peers need to exchange text messages in
 * rooms, this record is serializable.
 *
 * @param roomUUID The UUID of the room in which the message is exchanged.
 * @param author The peer who wrote the message.
 * @param content The content of the message.
 */
public record RoomText(UUID roomUUID, Peer author, String content) implements Serializable {}
