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
            String extractedData, extractedCommand;
            byte[] responseData;
            DatagramPacket responsePacket;
        
            while (true) {
                receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);

                receiveMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                divideStrings = receiveMessage.trim().split(";");
                extractedCommand = divideStrings[0];
                extractedData = divideStrings[1];
                
                InetAddress senderAddress = receivePacket.getAddress();
                int senderPort = receivePacket.getPort();
                
                //System.out.println("Received response from " + senderAddress + ":" + senderPort + ": " + receiveMessage);
                
                if (extractedCommand.equals("PING")) {
                    client.addPeer(new Peer(extractedData,senderPort,senderAddress));
                    responseMessage = "PONG;"+client.getUsername();
                    responseData = responseMessage.getBytes();
                    responsePacket = new DatagramPacket(responseData, responseData.length, senderAddress, senderPort);
                    socket.send(responsePacket);
                    //System.out.println("Sent response to " + senderAddress + ":" + senderPort + ": " + responseMessage);
                }
                else if (extractedCommand.equals("PONG")) {
                    client.addPeer(new Peer(extractedData, senderPort, senderAddress));
                }
                // PENG: mettere PING/PONG a monte di username
                else if (extractedCommand.equals("ROOM_MEMBER_START")) {
                    client.createRoomMembership(senderAddress, extractedData);
                    client.sendParticipationQueryMessage(extractedData);
                    //PROBLEM: i don't know how to handle packets containing the ips of other group members                    
                }
                else if (extractedCommand.equals("MEMBER?")) {
                    String confirmMembershipString;
                    byte[] confirmMembershipMessage;
                    DatagramPacket confirmMembershipPacket;
                    for (Room r : client.getParticipatingRooms()) {
                        if (extractedData.equals(r.getIdentifier())) {
                            confirmMembershipString = "MEMBER!;"+extractedData+";"+client.getUsername();
                            confirmMembershipMessage = confirmMembershipString.getBytes();
                            confirmMembershipPacket = new DatagramPacket(confirmMembershipMessage, confirmMembershipMessage.length, senderAddress, senderPort);
                            socket.send(confirmMembershipPacket);
                            break;
                        }
                    }    
                }
                else if (extractedCommand.equals("MEMBER!")) {
                    String memberUsername, roomId;
                    roomId = divideStrings[1];
                    memberUsername = divideStrings[2];
                    Peer p = new Peer(memberUsername, senderPort, senderAddress);
                    for (Room r : client.getParticipatingRooms()) {
                        if (roomId.equals(r.getIdentifier())) {
                            r.addPeer(p);
                            break;
                        }
                    }
                }

            }    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
