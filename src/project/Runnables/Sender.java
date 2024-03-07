package project.Runnables;

import project.CLI.CLI;
import project.Client;
import project.Messages.Message;

import java.io.IOException;
import java.net.*;

public class Sender implements Runnable{

    public static final int PORT_NUMBER = 9999;
    private final Client client;
    private DatagramSocket socket;

    public Sender(Client client){
        this.client = client;
        String ip;
        try {
            this.socket = new DatagramSocket();
            this.socket.setBroadcast(true);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public InetAddress getIPAddress(){
        return socket.getInetAddress();
    }

    @Override
    public void run() {

    }

    public void sendPacket(Message message) throws IOException {
        DatagramPacket responsePacket = new DatagramPacket(message.content(), message.getLength(), message.destinationAddress(), PORT_NUMBER);
        socket.send(responsePacket);
        CLI.printDebug("SENT: " + message.getHumanReadableContent() + ", TO: " + message.destinationAddress());
    }
}
