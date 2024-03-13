package project.Communication.Messages;

import project.Model.Peer;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;

public class MessageBuilder {

    private static final String NEW_FIELD = ";";
    private static final String NEW_PARAM = ",";

    public static Message ping(String userID, String username, InetAddress destinationAddress){
        return new Message(MessageType.PING, (MessageType.PING + NEW_FIELD + userID + NEW_PARAM + username).getBytes(), destinationAddress);
    }

    public static Message pong(String userID, String username, InetAddress destinationAddress) {
        return new Message(MessageType.PONG, (MessageType.PONG + NEW_FIELD + userID + NEW_PARAM + username).getBytes(), destinationAddress);
    }


    public static Message roomDelete(String processID, String roomUUID, InetAddress destinationAddress){
        return new Message(MessageType.ROOM_MEMBER, (MessageType.ROOM_DELETE + NEW_PARAM + processID + NEW_FIELD +
                roomUUID + NEW_FIELD).getBytes(), destinationAddress);
    }

    public static Message memberInfoRequest(String processID, String missingPeerUUID, String roomUUID, InetAddress destinationAddress){
        return new Message(MessageType.MEMBER_INFO_REQUEST, (MessageType.MEMBER_INFO_REQUEST + NEW_PARAM + processID + NEW_FIELD +
                missingPeerUUID + NEW_PARAM + roomUUID).getBytes(), destinationAddress);
    }

    public static Message memberInfoReply(String processID, Peer missingPeer, String roomUUID, InetAddress destinationAddress){
        return new Message(MessageType.MEMBER_INFO_REPLY, (MessageType.MEMBER_INFO_REQUEST + NEW_PARAM + processID + NEW_FIELD +
                missingPeer.getIdentifier().toString() + NEW_PARAM + missingPeer.getUsername() + NEW_PARAM + roomUUID).getBytes(), destinationAddress);
    }

    public static Message roomMessage(String roomUUID, Peer p, String content, InetAddress destinationAddress){
        return new Message(MessageType.ROOM_MEMBER, (MessageType.ROOM_MESSAGE + NEW_FIELD +
                roomUUID + NEW_PARAM + p.getUsername() + NEW_PARAM + content).getBytes(), destinationAddress);
    }

    public static Message roomMembership(String processID, String roomId, String multicastAddress, Integer multicastPort, String roomName, Peer creator, List<Peer> otherRoomMembers, InetAddress destinationAddress) {
        String memberList = "";
        memberList = memberList.concat(creator.getIdentifier().toString() + "/" + creator.getUsername() + "//");
        for(Peer p : otherRoomMembers) {
            memberList = memberList.concat(p.getIdentifier().toString() + "/" + p.getUsername() + "//");
        }
        return new Message(MessageType.ROOM_MEMBERSHIP, (MessageType.ROOM_MEMBERSHIP + NEW_FIELD +
                            roomId + NEW_PARAM + multicastAddress + NEW_PARAM + multicastPort.toString() + NEW_PARAM + memberList).getBytes(), destinationAddress);
    }

}
