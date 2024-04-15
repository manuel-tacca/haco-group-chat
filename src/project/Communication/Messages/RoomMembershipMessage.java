package project.Communication.Messages;

import project.Model.Room;

import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;

/**
 * This class represents the message that is sent by a peer who has created a room to notify the participants that they
 * have been added to that room. The related {@link Room} instance is also sent, so that everyone can access all the
 * necessary data.
 */
public class RoomMembershipMessage extends Message {

    private final Room room;
    private final UUID ackID;
    private final InetAddress sourceAddress;

    /**
     * Builds an instance of {@link RoomMembershipMessage}.
     *
     * @param vectorClock The vector clock attached to this message.
     * @param destinationAddress The destination address of the message.
     * @param destinationPort The destination port of the message.
     * @param room The created room.
     * @param ackID The ID of the AckWaitingList to acknowledge.
     */
    public RoomMembershipMessage(Map<UUID, Integer> vectorClock, InetAddress destinationAddress, int destinationPort, Room room, UUID ackID, InetAddress sourceAddress) {
        super(MessageType.ROOM_MEMBERSHIP, vectorClock, destinationAddress, destinationPort);
        this.room = room;
        this.ackID = ackID;
        this.sourceAddress = sourceAddress;
    }

    /**
     * Returns the created room.
     *
     * @return The created room.
     */
    public Room getRoom() {
        return room;
    }

    public UUID getAckID() {
        return ackID;
    }

    public InetAddress getSourceAddress() {
        return sourceAddress;
    }
}
