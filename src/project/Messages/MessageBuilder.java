package project.Messages;

import project.Peer;

public class MessageBuilder {

    public static byte[] ack(String data){
        return (MessageType.ACK + ";" + data).getBytes();
    }
    public static byte[] ping(String data){
        return (MessageType.PING + ";" + data).getBytes();
    }

    public static byte[] pong(String data) {
        return (MessageType.PONG + ";" + data).getBytes();
    }

    public static byte[] roomMemberStart(String roomUUID, String roomName, Peer p){
        return (MessageType.ROOM_MEMBER_START + ";" + roomUUID + "," + roomName + "," + p.getIdentifier()
                + "," + p.getUsername()).getBytes();
    }

    public static byte[] roomMember(String roomUUID, Peer p){
        return (MessageType.ROOM_MEMBER + ";" + roomUUID + "," + p.getIdentifier() + "," + p.getUsername()).getBytes();
    }

    public static byte[] roomMemberStop(String roomUUID, Peer p){
        return (MessageType.ROOM_MEMBER_STOP + ";" + roomUUID + "," + p.getIdentifier() + "," + p.getUsername()).getBytes();
    }

}
