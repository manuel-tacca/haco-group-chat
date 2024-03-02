package project.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SocketUtils {

    public static final int PORT_NUMBER = 9999;

    public static void sendPacket(DatagramSocket socket, byte[] data, InetAddress senderAddress) throws IOException {
        DatagramPacket responsePacket = new DatagramPacket(data, data.length, senderAddress, SocketUtils.PORT_NUMBER);
        socket.send(responsePacket);
    }

}
