package project.Communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import project.PacketHandler;

public class Listener implements Runnable{

    private PacketHandler packetHandler;

    public Listener(PacketHandler packetHandler){
        this.packetHandler = packetHandler;
    }

    @Override
    public void run() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(Sender.PORT_NUMBER);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        byte[] receivedData = new byte[1024];
        DatagramPacket receivedPacket;

        while (true) {

            // receive packet and extract message type and data
            receivedPacket = new DatagramPacket(receivedData, receivedData.length);
            try {
                socket.receive(receivedPacket);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                packetHandler.passMessage(receivedPacket);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
