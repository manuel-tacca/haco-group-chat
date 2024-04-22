package project.Communication;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import project.CLI.CLI;
import project.Communication.Messages.Message;

public class AckWaitingListMulticast {

    private UUID ackWaitingListID;
    private int numberOfTotAcks;
    private int numberOfReceivedAcks;
    private Timer timer;
    private TimerTask action;
    private long delay;
    private Boolean completed;

    public AckWaitingListMulticast(UUID ackWaitingListID, Message messageToResend, int numberOfTotAcks, Sender sender) {
        this.ackWaitingListID = ackWaitingListID;
        this.numberOfTotAcks = numberOfTotAcks;
        this.numberOfReceivedAcks = 0;
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
        this.delay = 5000;
        this.completed = false;
    }
    
    public void startTimer() {
        timer.scheduleAtFixedRate(action, 0, delay);
    }

    public void updateReceivedAcks() {
        numberOfReceivedAcks++;
        if (numberOfReceivedAcks == numberOfTotAcks) {
            timer.cancel();
            completed = true; 
            CLI.printDebug("acks received, timer canceled, success!");
        }
    }

    public Boolean getCompleted() {
        return completed;
    }

    public UUID getAckWaitingListID() {
        return ackWaitingListID; 
    }
}
