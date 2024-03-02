package project.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SocketUtils {

    public static final int PORT_NUMBER = 9999;

    public static void sendPacket(DatagramSocket socket, byte[] data, InetAddress senderAddress, int senderPort) throws IOException {
        DatagramPacket responsePacket = new DatagramPacket(data, data.length, senderAddress, senderPort);
        socket.send(responsePacket);
    }

}
