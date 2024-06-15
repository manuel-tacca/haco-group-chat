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

/**
 * This class is an {@link AckWaitingList} specialized in handling acks for unicast messages.
 */
public class AckWaitingListUnicast extends AckWaitingList{

    private final List<Message> messagesToResend;

    /**
     * Builds an instance of {@link AckWaitingListUnicast}.
     *
     * @param ackID the UUID of the ack.
     * @param sender a reference to the {@link Sender}.
     * @param messagesToResend the messages to resend in case an ack was not received.
     */
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

    /**
     * Updates the AckWaitingList upon reception of the acknowledgement from a peer.
     *
     * @param srcIP the IP address of the peer that has acknowledged the reception of the message.
     */
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
            completed = true;
        }
    }

    /**
     * Returns the messages whose acks have not been received yet.
     *
     * @return the messages whose acks have not been received yet.
     */
    public List<Message> getMessagesToResend() {
        return messagesToResend;
    }

}
