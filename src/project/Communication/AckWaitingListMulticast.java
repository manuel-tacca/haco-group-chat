package project.Communication;

import java.io.IOException;
import java.util.Timer;
import java.util.UUID;

import project.CLI.CLI;
import project.Communication.Messages.Message;

public class AckWaitingListMulticast extends AckWaitingList{

    private int numberOfTotAcks;
    private int numberOfReceivedAcks;
    private Message messageToResend;
    private Timer timer;
    private Sender sender;

    public AckWaitingListMulticast(UUID ackWaitingListID, Message messageToResend, int numberOfTotAcks, Sender sender) {
        
        super(ackWaitingListID, sender);
        
        this.numberOfTotAcks = numberOfTotAcks;
        this.numberOfReceivedAcks = 0;
        this.messageToResend = messageToResend;
    }

    public void updateReceivedAcks() {
        numberOfReceivedAcks++;
        if (numberOfReceivedAcks == numberOfTotAcks) {
            timer.cancel();
            CLI.printDebug("acks received, timer canceled, success!");
        }
    }

	@Override
	protected void onTimerExpired() {
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
}
