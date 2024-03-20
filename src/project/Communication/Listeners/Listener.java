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
import java.util.*;

/**
 * This abstract class is to be used as a base for specialized listeners. Listeners can be specialized in
 * a particular type of communication (e.g. {@link UnicastListener}, {@link MulticastListener}).
 */
public abstract class Listener implements Runnable{

    protected DatagramSocket socket;
    protected MessageHandler messageHandler;
    protected Thread thread;
    protected boolean isActive;
    protected Queue<Message> messageToDeliverQueue;

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
        this.messageToDeliverQueue = new LinkedList<>();
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

                    boolean canDeliver = true;
                    if (message.getVectorClock() != null) { // it's not a PING or a PONG
                        // Check causality: Compare received vector clock with local vector clock
                        UUID sender =
                        canDeliver = checkMessageCausality(message, sender);
                        CLI.printDebug("Vector clock received: " + message.getVectorClock().values());
                        CLI.printDebug("Local clock: " + messageHandler.getClient().getVectorClock().values());
                    }
                    if (!canDeliver) {
                        messageToDeliverQueue.offer(message);
                    } else {
                        messageHandler.handle(message);
                        if (message.getVectorClock() != null) {
                            CLI.printDebug("Vector clock received: " + message.getVectorClock().values());
                            CLI.printDebug("Local clock: " + messageHandler.getClient().getVectorClock().values());
                        }
                        checkDeferredMessages();
                    }
                }catch(PeerAlreadyPresentException ignored){
                } catch(Exception e){
                    CLI.appendNotification(new Notification(NotificationType.ERROR, e.getMessage()));
                }
            }
        }
    }

    private boolean checkMessageCausality(Message message) {
        boolean canDeliver = true;
        for (Map.Entry<UUID, Integer> entry : message.getVectorClock().entrySet()) {
            UUID uuid = entry.getKey();
            int timestamp = entry.getValue();
            int localTimestamp = messageHandler.getClient().getVectorClock().getOrDefault(uuid, 0);
            if (timestamp > localTimestamp) {
                canDeliver = false; // Deferred processing
                break;
            }
        }
        return canDeliver;
        // return true;
    }

    // Method to check and process deferred messages
    private void checkDeferredMessages() throws Exception {
        Iterator<Message> iterator = messageToDeliverQueue.iterator();
        while (iterator.hasNext()) {
            Message message = iterator.next();
            boolean canDeliver = checkMessageCausality(message);

            if (canDeliver) {
                iterator.remove();
                messageHandler.handle(message);
                checkDeferredMessages(); // prestare attenzione
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
