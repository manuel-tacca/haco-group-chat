package project.Communication.Listeners;

import java.io.IOException;
import java.net.*;

import project.CLI.CLI;
import project.Communication.Sender;
import project.Communication.PacketHandlers.PacketHandler;

public class Listener implements Runnable{

    private final PacketHandler packetHandler;
    private DatagramSocket socket;
    private boolean isActive;

    public Listener(PacketHandler packetHandler){
        this.packetHandler = packetHandler;
        isActive = true;
    }

    @Override
    public void run() {
        try {
            socket = new DatagramSocket(Sender.PORT_NUMBER);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        byte[] receivedData = new byte[1024];
        DatagramPacket receivedPacket;

        while (true) {

            // receive packet
            receivedPacket = new DatagramPacket(receivedData, receivedData.length);
            try {
                socket.receive(receivedPacket);
            } catch (IOException e) {
                if (isActive) {
                    throw new RuntimeException(e);
                }
            }

            try {
                packetHandler.handlePacket(receivedPacket);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void close(){
        isActive = false;
        if(socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
