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
public record RoomText(UUID roomUUID, Peer author, String content) implements Serializable {

    public boolean equals(RoomText roomText){
        return this.roomUUID.toString().equals(roomText.roomUUID.toString()) &&
                this.author.getIdentifier().toString().equals(roomText.author.getIdentifier().toString()) &&
                this.content.equals(roomText.content);
    }

}
