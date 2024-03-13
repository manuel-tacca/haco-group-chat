package project.Communication.PacketHandlers;

import project.CLI.CLI;
import project.Client;
import project.Communication.Messages.MessageParser;
import project.Communication.Messages.MessageType;
import project.Communication.Listeners.MulticastListener;
import project.DataStructures.MissingPeerRecoveryData;
import project.Model.Peer;
import project.Model.Room;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MulticastPacketHandler {

    private final Client client;
    private final List<MissingPeerRecoveryData> missingPeers;
    private final MulticastListener multicastListener;

    public MulticastPacketHandler(Client client, Room room) throws IOException {
        this.client = client;
        missingPeers = new ArrayList<>();
        multicastListener = new MulticastListener(room, this);
    }

    public MulticastListener getMulticastListener() {
        return multicastListener;
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
        if (command != null && data != null) {
            // extract information about the sender
            InetAddress senderAddress = receivedPacket.getAddress();
            int senderPort = receivedPacket.getPort();

            // execute action based on command
            switch (command) {
                case MessageType.ROOM_MEMBERSHIP:
                    handleRoomMembership(data, senderAddress);
                    break;
                case MessageType.ROOM_DELETE:
                    handleRoomDelete(data);
                    break;
                default:
                    break;
            }

        }
    }

    private void handleRoomMembership (String data, InetAddress senderAddress) throws Exception {
        String[] dataVector = data.split(",");
        String roomId = dataVector[0];
        String multicastIP = dataVector[1];
        String multicastPort = dataVector[2];
        String[] memberList = dataVector[3].split("//");

        List<Peer> peers = new ArrayList<>();
        for(String member : memberList) {
            String[] memParams = member.split("/");
            peers.add(new Peer(UUID.fromString(memParams[0]), memParams[1]));
        }
        //------------------------------------------
        for(Peer p : peers) {
            Optional<Peer> peer = client.getPeers().stream().filter(x -> x.getIdentifier().equals(p.getIdentifier())).findFirst();
            if(peer.isPresent()) {
                client.addRoomMember(roomId, peer.get());
            }
            else{
                missingPeers.add(new MissingPeerRecoveryData(p.getIdentifier().toString(), roomId));
                client.findMissingPeer(senderAddress,p.getIdentifier().toString(), roomId);
            }
        }
        // TODO: aggiungere localmente su client la nuova room con la lista dei peer
    }

    private void handleRoomDelete(String data) throws Exception {
        String[] dataVector = data.split(",");
        String roomID = dataVector[0];
        client.deleteRoom(roomID);
    }

    public void shutdown(){
        multicastListener.close();
    }

}
