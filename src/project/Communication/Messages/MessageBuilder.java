package project.Communication.Messages;

import project.Model.Peer;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;

public class MessageBuilder {

    private static final String FIELD_SEPARATOR = ";";
    private static final String PARAM_SEPARATOR = ",";

    public static Message ping(String userID, String username, InetAddress destinationAddress){
        return new Message(MessageType.PING, (MessageType.PING + FIELD_SEPARATOR + userID + PARAM_SEPARATOR + username).getBytes(), destinationAddress, null);
    }

    public static Message pong(String userID, String username, InetAddress destinationAddress) {
        return new Message(MessageType.PONG, (MessageType.PONG + FIELD_SEPARATOR + userID + PARAM_SEPARATOR + username).getBytes(), destinationAddress, null);
    }


    public static Message roomDelete(String processID, String roomUUID, InetAddress destinationAddress, UUID destinationProcessID){
        return new Message(MessageType.ROOM_MEMBER, (MessageType.ROOM_DELETE + PARAM_SEPARATOR + processID + FIELD_SEPARATOR +
                roomUUID + FIELD_SEPARATOR).getBytes(), destinationAddress, destinationProcessID);
    }

    public static Message memberInfoRequest(String processID, String missingPeerUUID, String roomUUID, InetAddress destinationAddress, UUID destinationProcessID){
        return new Message(MessageType.MEMBER_INFO_REQUEST, (MessageType.MEMBER_INFO_REQUEST + PARAM_SEPARATOR + processID + FIELD_SEPARATOR +
                missingPeerUUID + PARAM_SEPARATOR + roomUUID).getBytes(), destinationAddress, destinationProcessID);
    }

    public static Message memberInfoReply(String processID, Peer missingPeer, String roomUUID, InetAddress destinationAddress, UUID destinationProcessID){
        return new Message(MessageType.MEMBER_INFO_REPLY, (MessageType.MEMBER_INFO_REQUEST + PARAM_SEPARATOR + processID + FIELD_SEPARATOR +
                missingPeer.getIdentifier().toString() + PARAM_SEPARATOR + missingPeer.getUsername() + PARAM_SEPARATOR + roomUUID).getBytes(), destinationAddress, destinationProcessID);
    }

    public static Message roomMessage(String roomUUID, Peer p, String content, InetAddress destinationAddress, UUID destinationProcessID){
        return new Message(MessageType.ROOM_MEMBER, (MessageType.ROOM_MESSAGE + FIELD_SEPARATOR +
                roomUUID + PARAM_SEPARATOR + p.getUsername() + PARAM_SEPARATOR + content).getBytes(), destinationAddress, destinationProcessID);
    }

    public static Message roomMembership(String processID, String roomId, String multicastAddress, Integer multicastPort, String roomName, Peer creator, List<Peer> otherRoomMembers, InetAddress destinationAddress, UUID destinationProcessID) {
        String memberList = "";
        memberList = memberList.concat(creator.getIdentifier().toString() + "/" + creator.getUsername() + "//");
        for(Peer p : otherRoomMembers) {
            memberList = memberList.concat(p.getIdentifier().toString() + "/" + p.getUsername() + "//");
        }
        return new Message(MessageType.ROOM_MEMBERSHIP, (MessageType.ROOM_MEMBERSHIP + FIELD_SEPARATOR +
                            roomId + PARAM_SEPARATOR + multicastAddress + PARAM_SEPARATOR + multicastPort.toString() + PARAM_SEPARATOR + memberList).getBytes(), destinationAddress, destinationProcessID);
    }

}
