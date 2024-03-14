package project.Communication.PacketHandlers;

import project.CLI.CLI;
import project.Client;
import project.DataStructures.MissingPeerRecoveryData;
import project.Communication.Listeners.Listener;
import project.Communication.Messages.MessageParser;
import project.Communication.Messages.MessageType;
import project.Model.Peer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.*;

/* - runnable, parsa i pacchetti in entrata, chiama client per la logica
   - la coda di messaggi può essere sul listener, quando packetHandler è pronto lo notifica al listener
     per fetchare eventuali pacchetti
   - se il listener non ha pacchetti, che deve fare il packetHandler? da capire

*/
public class PacketHandler{ 

    private final Client client;
    private final List<MissingPeerRecoveryData> missingPeers;
    private final Listener listener;

    public PacketHandler(Client client) {
        this.client = client;
        missingPeers = new ArrayList<>();
        listener = new Listener(this);
    }

    public Listener getListener() {
        return listener;
    }

    public void handlePacket(DatagramPacket receivedPacket) throws Exception{

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
        if (isMessageValid(command, data)) {
            // extract information about the sender
            InetAddress senderAddress = receivedPacket.getAddress();
            unpack(command, data, senderAddress);
        }
    }

    private void unpack(String command, String data, InetAddress senderAddress) throws Exception {
        // unpack packet following specific rules based on command
        switch (command) {
            case MessageType.PING:
                unpackPing(data, senderAddress);
                break;
            case MessageType.PONG:
                unpackPong(data, senderAddress);
                break;
            case MessageType.ROOM_MEMBERSHIP:
                unpackRoomMembership(data);
                break;
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

    private void unpackPing(String data, InetAddress senderAddress) throws Exception {
        String[] dataVector = data.split(",");
        String userID = dataVector[0];
        String username = dataVector[1];
        client.handlePing(UUID.fromString(userID), username, senderAddress);
    }

    private void unpackPong(String data, InetAddress senderAddress) throws Exception{
        String[] dataVector = data.split(",");
        String userID = dataVector[0];
        String username = dataVector[1];
        client.handlePong(UUID.fromString(userID), username, senderAddress);
    }

    private void unpackRoomMembership(String data) throws Exception {
        String[] dataVector = data.split(",");
        String roomId = dataVector[0];
        String roomName = dataVector[1];
        String multicastAddress = dataVector[2];
        String[] memberIds = dataVector[3].split("/");
        List<UUID> uuidList = new ArrayList<>();
        for(String memberId: memberIds){
            uuidList.add(UUID.fromString(memberId));
        }
        client.handleRoomMembership(UUID.fromString(roomId), roomName, InetAddress.getByName(multicastAddress), uuidList);
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
        Peer peer = new Peer(UUID.fromString(peerID), peerUsername, senderAddress);
        /*client.addPeer(peer); //FIXME
        Optional<MissingPeerRecoveryData> recoveryData = missingPeers.stream().filter(x -> x.getPeerID().equals(peerID) && x.getRoomID().equals(roomID)).findFirst();
        if(recoveryData.isPresent()) {
            client.addRoomMember(recoveryData.get().getRoomID(), peer);
        }
        else{
            throw new RuntimeException();
        }*/
    }

    private boolean isMessageValid(String command, String data){
        return command != null && data != null;
    }

    public void shutdown(){
        listener.close();
    }

}
