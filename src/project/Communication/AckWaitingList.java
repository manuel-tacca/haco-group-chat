package project.Communication;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import project.Communication.Messages.Message;
import project.Communication.Messages.MessageType;
import project.Model.Peer;

public class AckWaitingList {

    private UUID ackWaitingListID;
    private MessageType messageType;
    private List<Message> messagesToResend; // maybe it should be done in client?
    private ScheduledExecutorService executor;
    private Sender sender;
    private Runnable action;
    private long delay;

    public AckWaitingList(UUID ackWaitingListID, MessageType messageType, List<Message> messagesToResend, Sender sender){
        this.ackWaitingListID = ackWaitingListID;
        this.messageType = messageType;
        this.messagesToResend = new ArrayList<>();
        this.messagesToResend.addAll(messagesToResend);
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.sender = sender;
        this.action = () -> {
            for(Message m : messagesToResend) {
                try {
                    sender.sendMessage(m);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        this.delay = 1000;
    }
    
    public UUID getAckWaitingListID() {
        return ackWaitingListID;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void startTimer() {
        executor.schedule(action, delay, TimeUnit.MILLISECONDS);
    }

    public void remove(InetAddress address) {  // maybe it should be done in client ???
        for (Message m : messagesToResend) {
            if (m.getDestinationAddress().equals(address)) {
                messagesToResend.remove(m);
            }
        }
        if (messagesToResend.isEmpty()) {
            executor.shutdown();
        }
    }
}
