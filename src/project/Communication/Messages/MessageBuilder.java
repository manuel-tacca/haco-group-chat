package project.Communication.Messages;

import project.Model.Peer;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;

public class MessageBuilder {

    private static final String NEW_FIELD = ";";
    private static final String NEW_PARAM = ",";
    private static final String SEPARATOR = "/";

    public static Message ping(UUID userUUID, String username, InetAddress destinationAddress, int destinationPort){
        return new Message(MessageType.PING, (MessageType.PING + NEW_FIELD + userUUID + NEW_PARAM + username).getBytes(), destinationAddress, destinationPort);
    }

    public static Message pong(UUID userID, String username, InetAddress destinationAddress, int destinationPort) {
        return new Message(MessageType.PONG, (MessageType.PONG + NEW_FIELD + userID + NEW_PARAM + username).getBytes(), destinationAddress, destinationPort);
    }

    public static Message roomMembership(UUID processUUID, UUID roomUUID, String roomName, InetAddress multicastAddress, Peer creator, List<Peer> otherRoomMembers, InetAddress destinationAddress, int destinationPort) {
        String memberList = "";
        for(Peer p : otherRoomMembers) {
            memberList = memberList.concat(p.getIdentifier().toString() + SEPARATOR);
        }
        memberList = memberList.substring(0, memberList.length() - 1); // removes useless separator
        return new Message(MessageType.ROOM_MEMBERSHIP, (MessageType.ROOM_MEMBERSHIP + NEW_PARAM + processUUID + NEW_FIELD +
                roomUUID + NEW_PARAM + roomName + NEW_PARAM + multicastAddress + NEW_PARAM + memberList).getBytes(), destinationAddress, destinationPort);
    }

    public static Message roomDelete(UUID processUUID, UUID roomUUID, InetAddress destinationAddress, int destinationPort){
        return new Message(MessageType.ROOM_MEMBER, (MessageType.ROOM_DELETE + NEW_PARAM + processUUID + NEW_FIELD +
                roomUUID + NEW_FIELD).getBytes(), destinationAddress, destinationPort);
    }

    public static Message memberInfoRequest(UUID processUUID, String missingPeerUUID, String roomUUID, InetAddress destinationAddress, int destinationPort){
        return new Message(MessageType.MEMBER_INFO_REQUEST, (MessageType.MEMBER_INFO_REQUEST + NEW_PARAM + processUUID + NEW_FIELD +
                missingPeerUUID + NEW_PARAM + roomUUID).getBytes(), destinationAddress, destinationPort);
    }

    public static Message memberInfoReply(UUID processUUID, Peer missingPeer, String roomUUID, InetAddress destinationAddress, int destinationPort){
        return new Message(MessageType.MEMBER_INFO_REPLY, (MessageType.MEMBER_INFO_REQUEST + NEW_PARAM + processUUID + NEW_FIELD +
                missingPeer.getIdentifier().toString() + NEW_PARAM + missingPeer.getUsername() + NEW_PARAM + roomUUID).getBytes(), destinationAddress, destinationPort);
    }

    public static Message roomMessage(UUID roomUUID, Peer p, String content, InetAddress destinationAddress, int destinationPort){
        return new Message(MessageType.ROOM_MEMBER, (MessageType.ROOM_MESSAGE + NEW_FIELD +
                roomUUID + NEW_PARAM + p.getUsername() + NEW_PARAM + content).getBytes(), destinationAddress, destinationPort);
    }

}
