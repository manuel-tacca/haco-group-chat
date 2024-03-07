package project.Messages;

import project.Client;
import project.Peer;

import java.net.InetAddress;

public class MessageBuilder {

    private static final String FIELD_SEPARATOR = ";";
    private static final String PARAM_SEPARATOR = ",";

    public static Message ack(String processID, int sequenceNumber, InetAddress destinationAddress){
        return new Message((MessageType.ACK + PARAM_SEPARATOR + processID + FIELD_SEPARATOR + FIELD_SEPARATOR +
                sequenceNumber).getBytes(), destinationAddress);
    }
    public static Message ping(String userID, String username, InetAddress destinationAddress){
        return new Message((MessageType.PING + FIELD_SEPARATOR + userID + PARAM_SEPARATOR + username).getBytes(), destinationAddress);
    }

    public static Message pong(String userID, String username, InetAddress destinationAddress) {
        return new Message((MessageType.PONG + FIELD_SEPARATOR + userID + PARAM_SEPARATOR + username).getBytes(), destinationAddress);
    }

    public static Message roomMemberStart(String processID, String roomUUID, String roomName, Peer p, int membersNum, InetAddress destinationAddress, int sequenceNumber){
        return new Message((MessageType.ROOM_MEMBER_START + PARAM_SEPARATOR + processID + FIELD_SEPARATOR +
                roomUUID + PARAM_SEPARATOR + roomName + PARAM_SEPARATOR + p.getIdentifier() + PARAM_SEPARATOR + p.getUsername() + PARAM_SEPARATOR + membersNum + FIELD_SEPARATOR +
                sequenceNumber).getBytes(), destinationAddress);
    }

    public static Message roomMember(String processID, String roomUUID, Peer p, InetAddress destinationAddress, int sequenceNumber){
        return new Message((MessageType.ROOM_MEMBER + PARAM_SEPARATOR + processID + FIELD_SEPARATOR +
                roomUUID + PARAM_SEPARATOR + p.getIdentifier() + PARAM_SEPARATOR + p.getUsername() + FIELD_SEPARATOR +
                sequenceNumber).getBytes(), destinationAddress);
    }

    public static Message memberInfoRequest(String processID, String missingPeerUUID, String roomUUID, InetAddress destinationAddress){
        return new Message((MessageType.MEMBER_INFO_REQUEST + PARAM_SEPARATOR + processID + FIELD_SEPARATOR +
                missingPeerUUID + PARAM_SEPARATOR + roomUUID).getBytes(), destinationAddress);
    }

    public static Message memberInfoReply(String processID, Peer missingPeer, String roomUUID, InetAddress destinationAddress){
        return new Message((MessageType.MEMBER_INFO_REQUEST + PARAM_SEPARATOR + processID + FIELD_SEPARATOR +
                missingPeer.getIdentifier().toString() + PARAM_SEPARATOR + missingPeer.getUsername() + PARAM_SEPARATOR + missingPeer.getIpAddress() + PARAM_SEPARATOR + missingPeer.getPort() + PARAM_SEPARATOR + roomUUID).getBytes(), destinationAddress);
    }

}
