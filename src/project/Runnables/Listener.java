package project.Runnables;

import project.CLI.CLI;
import project.Client;
import project.Messages.Message;
import project.Messages.MessageBuilder;
import project.Messages.MessageParser;
import project.Messages.MessageType;
import project.Peer;
import project.Utils.SocketUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Listener implements Runnable{

    private final Client client;
    private DatagramSocket socket;

    public Listener(Client client) {
        this.client = client;
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

                CLI.printDebug(command + " " + data);

                // execute action based on command
                switch(command){
                    case MessageType.ACK:
                        int sequenceNumber = MessageParser.extractSequenceNumber(receivedPacket);
                        handleAck(senderAddress, sequenceNumber);
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
                    default:
                        break;
                }

            }    
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleAck(InetAddress senderAddress, int sequenceNumber) throws Exception {
        client.acknowledge(sequenceNumber);
        Message response = MessageBuilder.ack(sequenceNumber, senderAddress);
        SocketUtils.sendPacket(client.getSocket(), response);
    }

    private void handlePing(String data, InetAddress senderAddress, int senderPort) throws Exception {
        String[] dataVector = data.split(",");
        String userID = dataVector[0];
        String username = dataVector[1];
        if(!userID.equals(client.getPeerData().getIdentifier().toString())) {
            client.addPeer(new Peer(userID, username, senderAddress, senderPort));
            Message response = MessageBuilder.pong(client.getPeerData().getIdentifier().toString(), client.getPeerData().getUsername(), senderAddress);
            SocketUtils.sendPacket(client.getSocket(), response);
        }
    }

    private void handlePong(String data, InetAddress senderAddress, int senderPort) throws Exception{
        String[] dataVector = data.split(",");
        String userID = dataVector[0];
        String username = dataVector[1];
        client.addPeer(new Peer(userID, username, senderAddress, senderPort));
    }

    private void handleRoomMemberStart(String data, InetAddress senderAddress, int senderPort) throws IOException {
        String[] dataVector = data.split(",");
        String roomID = dataVector[0];
        String roomName = dataVector[1];
        String peerID = dataVector[2];
        String peerUsername = dataVector[3];
        int membersNumber = Integer.parseInt(dataVector[4]);
        Peer peer = new Peer(peerID, peerUsername, senderAddress, senderPort);
        client.createRoomMembership(peer, roomID, roomName, membersNumber);
        sendAck(senderAddress);
    }

    private void handleRoomMember(String data, InetAddress senderAddress, int senderPort) throws Exception {
        String[] dataVector = data.split(",");
        String roomID = dataVector[0];
        String peerID = dataVector[1];
        String peerUsername = dataVector[2];
        Peer peer = new Peer(peerID, peerUsername, senderAddress, senderPort);
        client.addRoomMember(roomID, peer);
        sendAck(senderAddress);
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    private void sendAck(InetAddress destinationAddress) throws IOException{
        int sequenceNumber = client.getAndIncrementSequenceNumber();
        Message response = MessageBuilder.ack(sequenceNumber, destinationAddress);
        client.putInPending(sequenceNumber, response);
        SocketUtils.sendPacket(client.getSocket(), response);
    }

}
