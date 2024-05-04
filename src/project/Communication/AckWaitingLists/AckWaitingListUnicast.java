package project.Communication.AckWaitingLists;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.UUID;

import project.CLI.CLI;
import project.Communication.Messages.Message;
import project.Communication.Sender;
import project.Model.Notification;
import project.Model.NotificationType;

public class AckWaitingListUnicast extends AckWaitingList{

    private final List<Message> messagesToResend;

    public AckWaitingListUnicast(UUID ackID, Sender sender, List<Message> messagesToResend) {

        super(ackID, sender, 
            new TimerTask() {
                @Override
                public void run() {
                    for(Message m : messagesToResend) {
                        try {
                            sender.sendMessage(m);
                        } catch (IOException ignored) {}
                    }
                }
            }
        );
        this.messagesToResend = new ArrayList<>();
        this.messagesToResend.addAll(messagesToResend);
    }


    public void update(InetAddress srcIP) {

        if (srcIP == null) {
            return;
        }   

        for (Message m : messagesToResend) {
            if (m.getDestinationAddress().equals(srcIP)) {
                messagesToResend.remove(m);
                break;
            }
        }

        if (messagesToResend.isEmpty()) {
            timer.cancel();
            CLI.printDebug("acks received successfully, stopping timer!");
            isComplete = true;
        }
    }

    public List<Message> getMessagesToResend() {
        return messagesToResend;
    }

}
