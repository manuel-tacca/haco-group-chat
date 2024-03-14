package project.Communication.Listeners;

import java.io.IOException;
import java.net.*;
import java.util.UUID;

import project.Communication.NetworkUtils;
import project.Model.Room;
import project.Communication.PacketHandlers.MulticastPacketHandler;

public class MulticastListener implements Runnable {

    private final MulticastSocket multicastSocket;
    private NetworkInterface lanInterface;
    private final MulticastPacketHandler multicastPacketHandler;
    private final Room room;
    private boolean isActive;

    public MulticastListener(MulticastPacketHandler multicastPacketHandler, Room room) throws IOException {

        /*multicastSocket = new MulticastSocket();
        SocketAddress socketAddress = new InetSocketAddress(NetworkUtils.generateRandomMulticastAddress(), NetworkUtils.MULTICAST_PORT_NUMBER);
        try {
            multicastSocket.bind(socketAddress);
        }catch(SocketException e){
            System.out.println(Arrays.toString(e.getCause().getStackTrace()));
        }

        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (!networkInterface.isLoopback() && !networkInterface.isVirtual() && networkInterface.isUp()) {
                lanInterface = networkInterface;
                break;
            }
        }

        multicastSocket.joinGroup(socketAddress, lanInterface);*/

        this.multicastSocket = new MulticastSocket();
        multicastSocket.joinGroup(new InetSocketAddress(NetworkUtils.generateRandomMulticastAddress(),
                NetworkUtils.MULTICAST_PORT_NUMBER), NetworkUtils.getAvailableMulticastNetworkInterface());

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
