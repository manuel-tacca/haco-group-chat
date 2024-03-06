package project.Utils;

import project.Messages.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class SocketUtils {

    public static final int PORT_NUMBER = 9999;

    public static void sendPacket(DatagramSocket socket, Message message) throws IOException {
        DatagramPacket responsePacket = new DatagramPacket(message.content(), message.getLength(), message.destinationAddress(), SocketUtils.PORT_NUMBER);
        socket.send(responsePacket);
    }

}
