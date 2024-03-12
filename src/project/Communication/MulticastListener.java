package project.Communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.util.Enumeration;

import project.Model.Room;

public class MulticastListener implements Runnable {

    private Room room;
    private MulticastSocket socket;
    private NetworkInterface lanInterface;
    private SocketAddress socketAddress;

    public MulticastListener(Room room) throws IOException {

        this.room = room;
        socket = new MulticastSocket();
        socketAddress = new java.net.InetSocketAddress(room.getMulticastPort());
        socket.bind(socketAddress);

        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (!networkInterface.isLoopback() && !networkInterface.isVirtual() && networkInterface.isUp()) {
                lanInterface = networkInterface;
                break;
            }
        }

        socket.joinGroup(socketAddress, lanInterface);
    }

    @Override
    public void run() {
        byte[] receivedData = new byte[65000];
        DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
        while (true) {

            // receive packet and extract message type and data
            receivedPacket = new DatagramPacket(receivedData, receivedData.length);
            try {
                socket.receive(receivedPacket);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // TODO: need to understand how to handle packets
        }
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
    
}
