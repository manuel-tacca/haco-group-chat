package project.Communication.Messages;

import java.net.DatagramPacket;

public class MessageParser {

    public static String extractCommand(DatagramPacket packet){
        String message = new String(packet.getData(), 0, packet.getLength());
        return message.trim().split(";")[0].split(",")[0];
    }

    public static String extractProcessID(DatagramPacket packet){
        String message = new String(packet.getData(), 0, packet.getLength());
        return message.trim().split(";")[0].split(",")[1];
    }

    public static String extractData(DatagramPacket packet){
        String message = new String(packet.getData(), 0, packet.getLength());
        return message.trim().split(";")[1];
    }

    public static int extractSequenceNumber(DatagramPacket packet){
        String message = new String(packet.getData(), 0, packet.getLength());
        return Integer.parseInt(message.trim().split(";")[2]);
    }

}
