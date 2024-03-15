package project.Communication.PacketHandlers;

import project.CLI.CLI;
import project.Client;
import project.Communication.Messages.MessageParser;
import project.Communication.Messages.MessageType;
import project.Communication.Listeners.MulticastListener;
import project.Exceptions.InvalidParameterException;
import project.Model.Room;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.UUID;

public class MulticastPacketHandler {

    private final Client client;
    private final MulticastListener multicastListener;

    public MulticastPacketHandler(Client client, Room room) throws IOException {
        this.client = client;
        multicastListener = new MulticastListener(this, room);
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

            // execute action based on command
            switch (command) {
                case MessageType.ROOM_MESSAGE:
                    unpackRoomMessage(data);
                    break;
                case MessageType.ROOM_DELETE:
                    unpackRoomDelete(data);
                    break;
                default:
                    break;
            }

        }
    }

    private void unpackRoomMessage(String data) throws InvalidParameterException {
        String[] dataVector = data.split(",");
        String roomID = dataVector[0];
        String authorID = dataVector[1];
        String content = dataVector[2];
        client.handleRoomMessage(UUID.fromString(roomID), UUID.fromString(authorID), content);
    }

    private void unpackRoomDelete(String data){
        String[] dataVector = data.split(",");
        String roomID = dataVector[0];
        client.deleteRoom(roomID);
    }

    public void shutdown(){
        multicastListener.close();
    }

}
