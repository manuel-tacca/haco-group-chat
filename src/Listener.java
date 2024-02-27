import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Listener implements Runnable{
    private Client client;

    public Listener(Client client) {
        this.client = client;
    }
    
    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(9999);
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket;
            String receiveMessage, responseMessage;
            String[] divideStrings;
            String extractedUsername, extractedCommand;
            byte[] responseData;
            DatagramPacket responsePacket;
        
            while (true) {
                receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);

                receiveMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                divideStrings = receiveMessage.trim().split(";");
                extractedCommand = divideStrings[0];
                extractedUsername = divideStrings[1];
                
                InetAddress senderAddress = receivePacket.getAddress();
                int senderPort = receivePacket.getPort();
                
                System.out.println("Received response from " + senderAddress + ":" + senderPort + ": " + receiveMessage);
                
                if (extractedCommand.equals("PING")) {
                    client.addPeer(new Peer(extractedUsername,senderPort,senderAddress));
                    responseMessage = "PONG;"+client.getUsername();
                    responseData = responseMessage.getBytes();
                    responsePacket = new DatagramPacket(responseData, responseData.length, senderAddress, senderPort);
                    socket.send(responsePacket);
                    System.out.println("Sent response to " + senderAddress + ":" + senderPort + ": " + responseMessage);
                }
                else if (extractedCommand.equals("PONG")) {
                    client.addPeer(new Peer(extractedUsername, senderPort, senderAddress));
                }
            }    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
