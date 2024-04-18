package project.Communication;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import project.CLI.CLI;
import project.Communication.Messages.Message;
import project.Communication.Messages.MessageType;
import project.Model.Notification;
import project.Model.NotificationType;

public class AckWaitingList {

    private UUID ackWaitingListID;
    private MessageType messageType;
    private List<Message> messagesToResend; // maybe it should be done in client?
    private Timer timer;
    private Sender sender;
    private TimerTask action;
    private long delay;

    public AckWaitingList(UUID ackWaitingListID, MessageType messageType, List<Message> messagesToResend, Sender sender){
        this.ackWaitingListID = ackWaitingListID;
        this.messageType = messageType;
        this.messagesToResend = new ArrayList<>();
        this.messagesToResend.addAll(messagesToResend);
        // Initialize action to do when timer runs out: resend messagesToResend, and delay: 1s (1000 ms)
        this.timer = new Timer();
        this.sender = sender;
        this.action = new TimerTask() {
            public void run() {
                for(Message m : messagesToResend) {
                    try {
                        sender.sendMessage(m);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        this.delay = 50000;
    }
    
    public UUID getAckWaitingListID() {
        return ackWaitingListID;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void startTimer() {
        timer.schedule(action, delay);
    }

    public void remove(InetAddress address) {  // maybe it should be done in client ???
        for (Message m : messagesToResend) {
            if (m.getDestinationAddress().equals(address)) {
                messagesToResend.remove(m);
                break;
            }
        }
        if (messagesToResend.size() == 0) {
            timer.cancel();
            CLI.appendNotification(new Notification(NotificationType.SUCCESS, "all acks were received correctly"));
        }
    }
}
