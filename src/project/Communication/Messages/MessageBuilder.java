package project.Communication.Messages;

import project.Model.Peer;

import java.net.InetAddress;
import java.util.Set;
import java.util.UUID;

public class MessageBuilder {

    public static final String NEW_FIELD = ";";
    public static final String NEW_PARAM = ",";
    public static final String NEW_SUBFIELD = "||";
    public static final String NEW_SUBPARAM = "|";

    public static Message ping(UUID userUUID, String username, InetAddress destinationAddress, int destinationPort){
        return new Message(MessageType.PING, (MessageType.PING + NEW_FIELD + userUUID + NEW_PARAM + username).getBytes(), destinationAddress, destinationPort);
    }

    public static Message pong(UUID userID, String username, InetAddress destinationAddress, int destinationPort) {
        return new Message(MessageType.PONG, (MessageType.PONG + NEW_FIELD + userID + NEW_PARAM + username).getBytes(), destinationAddress, destinationPort);
    }

    public static Message roomMembership(UUID processUUID, UUID roomUUID, String roomName, InetAddress multicastAddress, Set<Peer> roomMembers, InetAddress destinationAddress, int destinationPort) {
        String memberList = "";
        for(Peer p : roomMembers) {
            memberList = memberList.concat(p.getIdentifier() + NEW_SUBPARAM + p.getUsername() + NEW_SUBPARAM + p.getIpAddress() + NEW_SUBFIELD);
        }
        memberList = memberList.substring(0, memberList.length() - 2); // removes useless NEW_SUBFIELD
        return new Message(MessageType.ROOM_MEMBERSHIP, (MessageType.ROOM_MEMBERSHIP + NEW_PARAM + processUUID + NEW_FIELD +
                roomUUID + NEW_PARAM + roomName + NEW_PARAM + multicastAddress + NEW_PARAM + memberList).getBytes(), destinationAddress, destinationPort);
    }

}
