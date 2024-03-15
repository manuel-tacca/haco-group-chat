package project.Model;

/**
 * This record represents a message that is exchanged in a room.
 *
 * @param author The peer who wrote the message.
 * @param content The content of the message.
 * @param writtenByMe Whether the message was written by the user or not.
 */
public record RoomMessage(Peer author, String content, boolean writtenByMe) {}