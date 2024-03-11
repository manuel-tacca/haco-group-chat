package project;

import project.CLI.CLI;
import project.DataStructures.MissingPeerRecoveryData;
import project.Exceptions.PeerAlreadyPresentException;
import project.Communication.Listener;
import project.Communication.Messages.Message;
import project.Communication.Messages.MessageBuilder;
import project.Communication.Messages.MessageParser;
import project.Communication.Messages.MessageType;
import project.Model.Peer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

/* - runnable, parsa i pacchetti in entrata, chiama client per la logica
   - la coda di messaggi può essere sul listener, quando packetHandler è pronto lo notifica al listener
     per fetchare eventuali pacchetti
   - se il listener non ha pacchetti, che deve fare il packetHandler? da capire

*/
public class PacketHandler{ 

    private final Client client;
    private DatagramSocket socket;
    private final List<MissingPeerRecoveryData> missingPeers;
    private Listener listener;
    private DatagramPacket receivedPacket;


    public PacketHandler(Client client) {
        this.client = client;
        missingPeers = new ArrayList<>();
        listener = new Listener(this);
        Thread fastThread = new Thread(listener);
        fastThread.start();
    }

    public void passMessage(DatagramPacket receivedPacket) throws Exception{
        this.receivedPacket = receivedPacket;
        String command = null;
        try{
            command = MessageParser.extractCommand(receivedPacket);
        }
        catch(ArrayIndexOutOfBoundsException ignored){}

        String processID = null;
        try{
            processID = MessageParser.extractProcessID(receivedPacket);
        }
        catch(ArrayIndexOutOfBoundsException ignored){}
        UUID processUUID = processID != null ? UUID.fromString(processID) : null;

        String data = null;
        try{
            data = MessageParser.extractData(receivedPacket);
        }
        catch(ArrayIndexOutOfBoundsException ignored){}

        int sequenceNumber = -1;
        try{
            sequenceNumber = MessageParser.extractSequenceNumber(receivedPacket);
        }
        catch(ArrayIndexOutOfBoundsException ignored){}

        if (command == null) {
            CLI.printDebug("MESSAGE DISCARDED: " + sequenceNumber);
        } else {
            CLI.printDebug("RECEIVED: " + command + ", " + data);
        }

        // if data is null, that means the packet was not formatted according to our rules
        if (command != null && data != null) {
            // extract information about the sender
            InetAddress senderAddress = receivedPacket.getAddress();
            int senderPort = receivedPacket.getPort();

            // execute action based on command
            switch (command) {
                
                case MessageType.PING:
                    handlePing(data, senderAddress, senderPort);
                    break;
                case MessageType.PONG:
                    handlePong(data, senderAddress, senderPort);
                    break;
                case MessageType.ROOM_MEMBER_START:
                    handleRoomMemberStart(data, sequenceNumber, senderAddress);
                    break;
                case MessageType.ROOM_MEMBER:
                    handleRoomMember(data, sequenceNumber, senderAddress);
                    break;
                case MessageType.ROOM_DELETE:
                    handleRoomDelete(data, sequenceNumber, senderAddress);
                case MessageType.MEMBER_INFO_REQUEST:
                    handleMemberInfoRequest(data, senderAddress);
                    break;
                case MessageType.MEMBER_INFO_REPLY:
                    handleMemberInfoReply(data, senderAddress);
                    break;
                default:
                    break;
            }

        }
    }

    private void handlePing(String data, InetAddress senderAddress, int senderPort) throws Exception {
        String[] dataVector = data.split(",");
        String userID = dataVector[0];
        String username = dataVector[1];
        if(!userID.equals(client.getPeerData().getIdentifier().toString())) {
            Message response = MessageBuilder.pong(client.getPeerData().getIdentifier().toString(), client.getPeerData().getUsername(), senderAddress);
            client.sendPacket(response);
            client.addPeer(new Peer(userID, username, senderAddress, senderPort));
        }
    }

    private void handlePong(String data, InetAddress senderAddress, int senderPort) throws Exception{
        String[] dataVector = data.split(",");
        String userID = dataVector[0];
        String username = dataVector[1];
        client.addPeer(new Peer(userID, username, senderAddress, senderPort));
    }

    private void handleRoomMemberStart(String data, int sequenceNumber, InetAddress senderAddress) throws IOException {
        String[] dataVector = data.split(",");
        String roomID = dataVector[0];
        String roomName = dataVector[1];
        String peerID = dataVector[2];
        String peerUsername = dataVector[3];
        int membersNumber = Integer.parseInt(dataVector[4]);
        Optional<Peer> peer = client.getPeers().stream().filter(x -> x.getIdentifier().toString().equals(peerID)).findFirst();
        if(peer.isPresent()) {
            client.createRoomMembership(peer.get(), roomID, roomName, membersNumber);
        }
        else{
            missingPeers.add(new MissingPeerRecoveryData(peerID, roomID, sequenceNumber));
            client.findMissingPeer(senderAddress, roomID, peerID);
        }
    }

    private void handleRoomMember(String data, int sequenceNumber, InetAddress senderAddress) throws Exception {
        String[] dataVector = data.split(",");
        String roomID = dataVector[0];
        String peerID = dataVector[1];
        String peerUsername = dataVector[2];
        Optional<Peer> peer = client.getPeers().stream().filter(x -> x.getIdentifier().toString().equals(peerID)).findFirst();
        if(peer.isPresent()) {
            client.addRoomMember(roomID, peer.get());
        }
        else{
            missingPeers.add(new MissingPeerRecoveryData(peerID, roomID, sequenceNumber));
            client.findMissingPeer(senderAddress, roomID, peerID);
        }
    }

    private void handleRoomDelete(String data, int sequenceNumber, InetAddress senderAddress) throws Exception {
        String[] dataVector = data.split(",");
        String roomID = dataVector[0];
        client.deleteRoom(roomID);
    }

    private void handleMemberInfoRequest(String data, InetAddress senderAddress) throws IOException {
        String[] dataVector = data.split(",");
        String peerID = dataVector[0];
        String roomID = dataVector[1];
        Optional<Peer> peer = client.getPeers().stream().filter(x -> x.getIdentifier().toString().equals(peerID)).findFirst();
        /*if (peer.isPresent()){
            Message reply = MessageBuilder.memberInfoReply(client.getProcessID(), peer.get(), roomID, senderAddress, );
            client.sendPacket(reply);
        }
        else{
            throw new RuntimeException();
        }*/ //FIXME
    }

    private void handleMemberInfoReply(String data, InetAddress senderAddress) throws Exception {
        String[] dataVector = data.split(",");
        String peerID = dataVector[0];
        String peerUsername = dataVector[1];
        String peerIP = dataVector[2];
        String peerPort = dataVector[3];
        String roomID = dataVector[4];
        Peer peer = new Peer(peerID, peerUsername, InetAddress.getByName(peerIP), Integer.parseInt(peerPort));
        client.addPeer(peer);
        Optional<MissingPeerRecoveryData> recoveryData = missingPeers.stream().filter(x -> x.getPeerID().equals(peerID) && x.getRoomID().equals(roomID)).findFirst();
        if(recoveryData.isPresent()) {
            client.addRoomMember(recoveryData.get().getRoomID(), peer);
        }
        else{
            throw new RuntimeException();
        }
    }

    public DatagramSocket getSocket() {
        return socket;
    }

}
