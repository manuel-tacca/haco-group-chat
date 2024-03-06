package project.Messages;

import project.Client;
import project.Peer;

import java.net.InetAddress;

public class MessageBuilder {

    private static final String FIELD_SEPARATOR = ";";
    private static final String PARAM_SEPARATOR = ",";

    public static Message ack(int sequenceNumber, InetAddress destinationAddress){
        return new Message((MessageType.ACK + FIELD_SEPARATOR + FIELD_SEPARATOR + sequenceNumber).getBytes(), destinationAddress);
    }
    public static Message ping(String userID, String username, InetAddress destinationAddress){
        return new Message((MessageType.PING + FIELD_SEPARATOR + userID + PARAM_SEPARATOR + username).getBytes(), destinationAddress);
    }

    public static Message pong(String userID, String username, InetAddress destinationAddress) {
        return new Message((MessageType.PONG + FIELD_SEPARATOR + userID + PARAM_SEPARATOR + username).getBytes(), destinationAddress);
    }

    public static Message roomMemberStart(String roomUUID, String roomName, Peer p, int membersNum, InetAddress destinationAddress, int sequenceNumber){
        return new Message((MessageType.ROOM_MEMBER_START + FIELD_SEPARATOR +
                roomUUID + PARAM_SEPARATOR + roomName + PARAM_SEPARATOR + p.getIdentifier() + PARAM_SEPARATOR + p.getUsername() + PARAM_SEPARATOR + membersNum + FIELD_SEPARATOR +
                sequenceNumber).getBytes(), destinationAddress);
    }

    public static Message roomMember(String roomUUID, Peer p, InetAddress destinationAddress){
        return new Message((MessageType.ROOM_MEMBER + FIELD_SEPARATOR +
                roomUUID + PARAM_SEPARATOR + p.getIdentifier() + PARAM_SEPARATOR + p.getUsername()).getBytes(), destinationAddress);
    }

}
