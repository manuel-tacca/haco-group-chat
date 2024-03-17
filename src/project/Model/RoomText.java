package project.Model;

import java.io.Serializable;
import java.util.UUID;

/**
 * This class represents a message that is exchanged in a room. Since peers need to exchange messages in rooms,
 * this class is serializable.
 */
public class RoomText implements Serializable {

    private final UUID roomUUID;
    private final Peer author;
    private final String content;
    private boolean writtenByMe;

    public RoomText(UUID roomUUID, Peer author, String content, boolean writtenByMe) {
        this.roomUUID = roomUUID;
        this.author = author;
        this.content = content;
        this.writtenByMe = writtenByMe;
    }

    public UUID getRoomUUID() {
        return roomUUID;
    }

    public Peer getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public boolean isWrittenByMe() {
        return writtenByMe;
    }

    public void setWrittenByMe(boolean writtenByMe) {
        this.writtenByMe = writtenByMe;
    }
}