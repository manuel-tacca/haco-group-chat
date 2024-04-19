package project.Communication;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import project.CLI.CLI;
import project.Communication.Messages.Message;

public class AckWaitingListUnicast {

    private UUID ackWaitingListID;
    private List<Message> messagesToResend; // maybe it should be done in client?
    private Timer timer;
    private Sender sender;
    private TimerTask action;
    private long delay;

    public AckWaitingListUnicast(UUID ackWaitingListID, List<Message> messagesToResend, Sender sender){
        this.ackWaitingListID = ackWaitingListID;
        this.messagesToResend = new ArrayList<>();
        this.messagesToResend.addAll(messagesToResend);
        this.timer = new Timer();
        this.sender = sender;
        this.action = new TimerTask() {
            public void run() {
                CLI.printDebug("Action triggered!");
                if (messagesToResend.size() == 0) {
                    return;
                }
                for(Message m : messagesToResend) {
                    try {
                        sender.sendMessage(m);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        this.delay = 1000;
    }
    
    public UUID getAckWaitingListID() {
        return ackWaitingListID;
    }

    public void startTimer() {
        timer.scheduleAtFixedRate(action, 0, delay);
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
            CLI.printDebug("acks received, timer canceled, success!");
        }
    }
}
