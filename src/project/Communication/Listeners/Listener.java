package project.Communication.Listeners;

import project.CLI.CLI;
import project.Communication.Messages.Message;
import project.Communication.NetworkUtils;
import project.Communication.MessageHandlers.MessageHandler;
import project.Exceptions.PeerAlreadyPresentException;
import project.Model.Notification;
import project.Model.NotificationType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * This abstract class is to be used as a base for specialized listeners. Listeners can be specialized in
 * a particular type of communication (e.g. {@link UnicastListener}, {@link MulticastListener}).
 */
public abstract class Listener implements Runnable{

    protected DatagramSocket socket;
    protected MessageHandler messageHandler;
    protected Thread thread;
    protected boolean isActive;

    /**
     * Sets the parameters that are common to every {@link Listener}.
     *
     * @param socket The socket that will be receiving packets.
     * @param messageHandler The object that will handle the received messages.
     */
    public Listener(DatagramSocket socket, MessageHandler messageHandler){
        this.socket = socket;
        this.messageHandler = messageHandler;
        this.isActive = true;
        this.thread = new Thread(this);
        this.thread.start();
    }

    /**
     * Continuously waits for new packets from the LAN. Upon receipt, they are deserialized and forwarded to a
     * {@link MessageHandler}.
     */
    @Override
    public void run(){

        byte[] buffer = new byte[NetworkUtils.BUF_DIM];
        DatagramPacket receivedPacket;

        while (isActive) {

            // receive packet
            receivedPacket = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(receivedPacket);
            } catch (IOException e) {
                if (isActive) {
                    throw new RuntimeException(e);
                }
            }

            if(isActive && !receivedPacket.getAddress().equals(messageHandler.getClientIpAddress())) {
                try {
                    ByteArrayInputStream bais = new ByteArrayInputStream(receivedPacket.getData());
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    Message message = (Message) ois.readObject();
                    CLI.printDebug("RECEIVED: " + message.getType() + "\nFROM: " + receivedPacket.getAddress());
                    messageHandler.handle(message);
                }catch(PeerAlreadyPresentException ignored){
                } catch(Exception e){
                    CLI.appendNotification(new Notification(NotificationType.ERROR, e.getMessage()));
                }
            }
        }
    }

    /**
     * Closes the socket that is listening for packets from the LAN.
     *
     * @throws IOException If any sort of I/O error occurs.
     */
    public void close() throws IOException {
        isActive = false;
        if(socket != null && !socket.isClosed()) {
            socket.close();
        }
        thread.interrupt();
    }

}
