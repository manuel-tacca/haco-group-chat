package project.Model;

import project.Communication.Messages.RoomTextMessage;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.*;

/**
 * This class represents a room in which a user who has created it or joined it can chat. Each room has a fixed
 * list of participants, that is decided a creation time and can no longer be modified. Only the user who created
 * the room can delete it. Since peers need to exchange information about rooms, this class is serializable.
 */
public class Room implements Serializable {
    private final UUID identifier;
    private final String name;
    private final Set<Peer> roomMembers;
    private final InetAddress multicastAddress;
    private final List<RoomText> roomMessages;
    private final VectorClock roomVectorClock;
    private final Queue<RoomTextMessage> messageToDeliverQueue;

    /**
     * Builds an instance of the room. Since no UUID is requested, this constructor should be called only by the peer
     * who creates the room (the UUID is randomly generated).
     *
     * @param name The name of the room.
     * @param roomMembers The members of the room.
     * @param multicastAddress The multicast address to which messages will be sent.
     */
    public Room(String name, Set<Peer> roomMembers, InetAddress multicastAddress){
        this.identifier = UUID.randomUUID();
        this.name = name;
        this.multicastAddress = multicastAddress;
        this.roomMembers = roomMembers;
        this.roomMessages = new ArrayList<>();
        this.roomVectorClock = new VectorClock();
        this.messageToDeliverQueue = new LinkedList<>();
        for ( Peer peer : roomMembers ) {
            roomVectorClock.add(peer.getIdentifier(), 0);
        }
    }

    // GETTERS

    /**
     * Returns the name of the room.
     *
     * @return The name of the room.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the set of the members of the room.
     *
     * @return The set of the members of the room.
     */
    public Set<Peer> getRoomMembers() {
        return roomMembers;
    }

    /**
     * Returns the identifier (UUID) of the room.
     *
     * @return The UUID of the room.
     */
    public UUID getIdentifier(){
        return identifier;
    }

    /**
     * Returns the multicast address of the room.
     *
     * @return The multicast address of the room.
     */
    public InetAddress getMulticastAddress() {
        return this.multicastAddress;
    }

    /**
     * Returns the list of messages exchanged since the room's creation.
     *
     * @return The list of messages exchanged since the room's creation.
     */
    public List<RoomText> getRoomMessages() {
        return roomMessages;
    }

    /**
     * Returns the room's vector clock.
     *
     * @return The room's vector clock.
     */
    public VectorClock getRoomVectorClock() { return roomVectorClock; }

    /**
     * Returns the room's messageToDeliverQueue.
     *
     * @return The room's messageToDeliverQueue.
     */
    public Queue<RoomTextMessage> getMessageToDeliverQueue() { return messageToDeliverQueue; }

    // PUBLIC METHODS

    /**
     * Adds a message to the room's chronology.
     *
     * @param roomText The message to add.
     */
    public void addRoomText(RoomText roomText){
        roomMessages.add(roomText);
    }

    /**
     * Updates the local vector clock accordingly to the one received as a parameter.
     * 
     * @param vectorClockReceived
     */
    public void updateVectorClock(VectorClock vectorClockReceived) {
        for (UUID uuid : roomVectorClock.getKeys()) {
            roomVectorClock.replace(uuid, Math.max(roomVectorClock.getValue(uuid), vectorClockReceived.getValue(uuid)));
        }
    }

    public void incrementVectorClock(UUID senderUUID) {
        roomVectorClock.replace(senderUUID, roomVectorClock.getValue(senderUUID)+1);
    }

}
