package project.Communication;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import project.CLI.CLI;
import project.Communication.Messages.Message;

public class AckWaitingListMulticast {

    private UUID ackWaitingListID;
    private Message messageToResend;
    private int numberOfTotAcks;
    private int numberOfReceivedAcks;
    private Timer timer;
    private Sender sender;
    private TimerTask action;
    private long delay;

    public AckWaitingListMulticast(UUID ackWaitingListID, Message messageToResend, int numberOfTotAcks, Sender sender) {
        this.ackWaitingListID = ackWaitingListID;
        this.messageToResend = messageToResend;
        this.numberOfTotAcks = numberOfTotAcks;
        this.numberOfReceivedAcks = 0;
        this.sender = sender;
        this.timer = new Timer();
        this.action = new TimerTask() {
            public void run() {
                
                CLI.printDebug("Action triggered!");
                
                if (numberOfTotAcks == numberOfReceivedAcks) {
                    return;
                }
                
                try {
                    sender.sendMessage(messageToResend);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        this.delay = 1000;
    }
    
    public void startTimer() {
        timer.schedule(action, delay);
    }

    public void updateReceivedAcks() {
        numberOfReceivedAcks++;
        if (numberOfReceivedAcks == numberOfTotAcks) {
            timer.cancel();
            CLI.printDebug("acks received, timer canceled, success!");
        }
    }

    public UUID getAckWaitingListID() {
        return ackWaitingListID; 
    }
}
