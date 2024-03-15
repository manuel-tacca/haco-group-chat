package project.Communication.PacketHandlers;

import project.CLI.CLI;
import project.Client;
import project.Communication.Messages.MessageBuilder;
import project.Communication.Listeners.Listener;
import project.Communication.Messages.MessageParser;
import project.Communication.Messages.MessageType;
import project.Model.Peer;

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
    private final Listener listener;

    public PacketHandler(Client client) {
        this.client = client;
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
            default:
                break;
        }
    }

    private void unpackPing(String data, InetAddress senderAddress) throws Exception {
        String[] dataVector = data.split(MessageBuilder.NEW_PARAM);
        String userID = dataVector[0];
        String username = dataVector[1];
        client.handlePing(UUID.fromString(userID), username, senderAddress);
    }

    private void unpackPong(String data, InetAddress senderAddress) throws Exception{
        String[] dataVector = data.split(MessageBuilder.NEW_PARAM);
        String userID = dataVector[0];
        String username = dataVector[1];
        client.handlePong(UUID.fromString(userID), username, senderAddress);
    }

    private void unpackRoomMembership(String data) throws Exception {
        String[] dataVector = data.split(MessageBuilder.NEW_PARAM);
        String roomId = dataVector[0];
        String roomName = dataVector[1];
        String multicastAddress = dataVector[2];
        String[] membersInfo = dataVector[3].split(MessageBuilder.NEW_SUBFIELD);
        Set<Peer> peers = new HashSet<>();
        for(String memberInfo: membersInfo){
            String[] infos = memberInfo.split(MessageBuilder.NEW_SUBPARAM);
            peers.add(new Peer(UUID.fromString(infos[0]), infos[1], InetAddress.getByName(infos[2])));
        }
        client.handleRoomMembership(UUID.fromString(roomId), roomName, InetAddress.getByName(multicastAddress), peers);
    }


    private boolean isMessageValid(String command, String data){
        return command != null && data != null;
    }

    public void shutdown(){
        listener.close();
    }

}
