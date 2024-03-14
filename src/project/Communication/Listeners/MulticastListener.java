package project.Communication.Listeners;

import java.io.IOException;
import java.net.*;
import java.util.UUID;

import project.Communication.NetworkUtils;
import project.Model.Room;
import project.Communication.PacketHandlers.MulticastPacketHandler;

public class MulticastListener implements Runnable {

    private final MulticastSocket multicastSocket;
    private final MulticastPacketHandler multicastPacketHandler;
    private final Room room;
    private boolean isActive;

    public MulticastListener(MulticastPacketHandler multicastPacketHandler, Room room) throws IOException {
        this.multicastSocket = new MulticastSocket();
        multicastSocket.joinGroup(new InetSocketAddress(room.getMulticastAddress(),
                room.getMulticastPort()), NetworkUtils.getAvailableMulticastIPv4NetworkInterface());

        this.multicastPacketHandler = multicastPacketHandler;
        this.room = room;

        isActive = true;
    }

    public UUID getRoomIdentifier(){
        return room.getIdentifier();
    }

    @Override
    public void run() {
        byte[] receivedData = new byte[65000];
        DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
        while (true) {

            // receive packet and extract message type and data
            receivedPacket = new DatagramPacket(receivedData, receivedData.length);
            try {
                multicastSocket.receive(receivedPacket);
            } catch (IOException e) {
                if (isActive) {
                    throw new RuntimeException(e);
                }
            }

            try {
                multicastPacketHandler.handlePacket(receivedPacket);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void close(){
        isActive = false;
        if(multicastSocket != null && !multicastSocket.isClosed()) {
            multicastSocket.close();
        }
    }
    
}
