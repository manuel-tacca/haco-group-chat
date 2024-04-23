package project.Communication;

import project.CLI.CLI;
import project.Communication.Messages.Message;
import project.Communication.Messages.MessageType;
import project.Communication.Messages.RoomTextMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;

/**
 * This class sends any kind of packet to the LAN. Before doing so, it serializes its content using Java's
 * serialization.
 */
public class Sender{

    private final DatagramSocket socket;

    /**
     * Builds an instance of the Sender.
     */
    public Sender(){
        try {
            this.socket = new DatagramSocket();
            this.socket.setBroadcast(true);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sends any given message to the LAN. The message may be sent in unicast, multicast, or broadcast, according to
     * how the message itself was built.
     *
     * @param message The message to send.
     * @throws IOException If any I/O error occurs.
     */
    public void sendMessage(Message message) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(message);
        byte[] data = baos.toByteArray();
        socket.send(new DatagramPacket(data, data.length, message.getDestinationAddress(), message.getDestinationPort()));
        CLI.printDebug("SENT: " + message.getType() + "(length: " + data.length + ")" + "\nTO: " + message.getDestinationAddress());
        if (message.getType() == MessageType.ROOM_TEXT) {
            RoomTextMessage debug = (RoomTextMessage) message;
            // CLI.printDebug("Local vector clock: " + debug.getVectorClock().values());
        }
    }
}
