package project.Messages;

import project.Client;
import project.Peer;

import java.net.InetAddress;

public class MessageBuilder {

    private static final String FIELD_SEPARATOR = ";";
    private static final String PARAM_SEPARATOR = ",";

    public static Message ack(int sequenceNumber, InetAddress destinationAddress){
        return new Message((MessageType.ACK + FIELD_SEPARATOR + sequenceNumber).getBytes(), destinationAddress);
    }
    public static Message ping(String data, InetAddress destinationAddress){
        return new Message((MessageType.PING + FIELD_SEPARATOR + data).getBytes(), destinationAddress);
    }

    public static Message pong(String data, InetAddress destinationAddress) {
        return new Message((MessageType.PONG + FIELD_SEPARATOR + data).getBytes(), destinationAddress);
    }

    public static Message roomMemberStart(String roomUUID, String roomName, Peer p, int sequenceNumber, InetAddress destinationAddress){
        return new Message((MessageType.ROOM_MEMBER_START + FIELD_SEPARATOR +
                roomUUID + PARAM_SEPARATOR + roomName + PARAM_SEPARATOR + p.getIdentifier() + PARAM_SEPARATOR + p.getUsername() + FIELD_SEPARATOR +
                sequenceNumber).getBytes(), destinationAddress);
    }

    public static Message roomMember(String roomUUID, Peer p, InetAddress destinationAddress){
        return new Message((MessageType.ROOM_MEMBER + FIELD_SEPARATOR +
                roomUUID + PARAM_SEPARATOR + p.getIdentifier() + PARAM_SEPARATOR + p.getUsername()).getBytes(), destinationAddress);
    }

}
