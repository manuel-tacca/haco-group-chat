package project.Communication.Listeners;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

import project.Model.Room;
import project.Communication.PacketHandlers.MulticastPacketHandler;

public class MulticastListener implements Runnable {

    private Room room;
    private final MulticastSocket multicastSocket;
    private NetworkInterface lanInterface;
    private final MulticastPacketHandler multicastPacketHandler;
    private boolean isActive;

    public MulticastListener(Room room, MulticastPacketHandler multicastPacketHandler) throws IOException {

        this.room = room;
        multicastSocket = new MulticastSocket();
        SocketAddress socketAddress = new java.net.InetSocketAddress(room.getMulticastPort());
        multicastSocket.bind(socketAddress);

        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (!networkInterface.isLoopback() && !networkInterface.isVirtual() && networkInterface.isUp()) {
                lanInterface = networkInterface;
                break;
            }
        }

        multicastSocket.joinGroup(socketAddress, lanInterface);

        this.multicastPacketHandler = multicastPacketHandler;

        isActive = true;
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
