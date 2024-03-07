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
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), PORT_NUMBER);
            ip = socket.getLocalAddress().getHostAddress();
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }
        CLI.printDebug(ip);
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
