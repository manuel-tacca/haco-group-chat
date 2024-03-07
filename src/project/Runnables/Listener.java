package project.Runnables;

import project.Client;
import project.Exceptions.InvalidParameterException;
import project.Messages.MessageBuilder;
import project.Messages.MessageParser;
import project.Messages.MessageType;
import project.Peer;
import project.Utils.SocketUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class Listener implements Runnable{

    private final Client client;
    private DatagramSocket socket;

    public Listener(Client client) {
        this.client = client;
    }

    public DatagramSocket getSocket() {
        return socket;
    }
    
    @Override
    public void run() {
        try {
            socket = new DatagramSocket(SocketUtils.PORT_NUMBER);
            byte[] receivedData = new byte[1024];
            DatagramPacket receivedPacket;

            while (true) {

                // receive packet and extract message type and data
                receivedPacket = new DatagramPacket(receivedData, receivedData.length);
                socket.receive(receivedPacket);
                String command = MessageParser.extractCommand(receivedPacket);
                String data = MessageParser.extractData(receivedPacket);

                // extract information about the sender
                InetAddress senderAddress = receivedPacket.getAddress();
                int senderPort = receivedPacket.getPort();

                // execute action based on command
                switch(command){
                    case MessageType.ACK:
                        //handleAck(data, senderAddress, senderPort);
                        break;
                    case MessageType.PING:
                        handlePing(data, senderAddress, senderPort);
                        break;
                    case MessageType.PONG:
                        handlePong(data, senderAddress, senderPort);
                        break;
                    case MessageType.ROOM_MEMBER_START:
                        handleRoomMemberStart(data, senderAddress, senderPort);
                        break;
                    case MessageType.ROOM_MEMBER:
                        handleRoomMember(data, senderAddress, senderPort);
                        break;
                    case MessageType.ROOM_MEMBER_STOP:
                        //handleRoomMemberStop(data, senderAddress, senderPort);
                        break;
                    case MessageType.ROOM_MESSAGE:
                        handleRoomMessage(data, senderAddress, senderPort);
                    default:
                        break;
                }

            }    
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleRoomMessage(String data, InetAddress senderAddress, int senderPort) {
        String[] dataVector = data.split(",");
        String roomID = dataVector[0];
        String peerUsername = dataVector[1];
        String content = dataVector[2];
        if (client.getCurrentlyDisplayedRoom().getIdentifier().toString().equals(roomID)) {
            System.out.print(peerUsername+": ");
            System.out.println(content);
        }
        else {
            if (!client.getRoomMessagesMap().containsKey(roomID)) {
                client.getRoomMessagesMap().put(roomID, new StringBuilder());
            }
            client.getRoomMessagesMap().get(roomID).append(peerUsername).append(": ").append(content).append("\n");
        }
    }

    private void handleAck(String data, InetAddress senderAddress, int senderPort) throws InvalidParameterException{
        String[] dataVector = data.split(",");
        String referredCommand = dataVector[0];
        switch(referredCommand){
            case MessageType.ROOM_MEMBER_START:
            case MessageType.ROOM_MEMBER:
            case MessageType.ROOM_MEMBER_STOP:
                String roomID = dataVector[1];
                String userID = dataVector[2];
                client.handleAck(roomID, userID);
                break;
            default:
                break;
        }
    }

    private void handlePing(String data, InetAddress senderAddress, int senderPort) throws Exception {
        String[] dataVector = data.split(",");
        String userID = dataVector[0];
        String username = dataVector[1];
        if(!userID.equals(client.getPeerData().getIdentifier().toString())) {
            client.addPeer(new Peer(userID, username, senderAddress, senderPort));
            byte[] response = MessageBuilder.pong(client.getPeerData().getUsername());
            SocketUtils.sendPacket(client.getSocket(), response, senderAddress);
        }
    }

    private void handlePong(String data, InetAddress senderAddress, int senderPort) throws Exception{
        client.addPeer(new Peer(data, senderAddress, senderPort));
    }

    private void handleRoomMemberStart(String data, InetAddress senderAddress, int senderPort) throws IOException {
        String[] dataVector = data.split(",");
        String roomID = dataVector[0];
        String roomName = dataVector[1];
        String peerID = dataVector[2];
        String peerUsername = dataVector[3];
        Peer peer = new Peer(peerID, peerUsername, senderAddress, senderPort);
        client.createRoomMembership(peer, roomID, roomName);
    }

    private void handleRoomMember(String data, InetAddress senderAddress, int senderPort) throws Exception {
        String[] dataVector = data.split(",");
        String roomID = dataVector[0];
        String peerID = dataVector[1];
        String peerUsername = dataVector[2];
        Peer peer = new Peer(peerID, peerUsername, senderAddress, senderPort);
        client.addRoomMember(roomID, peer);
        String responseString = MessageType.ROOM_MEMBER + "," + roomID + "," + client.getPeerData().getIdentifier();
        byte[] response = MessageBuilder.ack(responseString);
        SocketUtils.sendPacket(client.getSocket(), response, senderAddress);
    }

}
