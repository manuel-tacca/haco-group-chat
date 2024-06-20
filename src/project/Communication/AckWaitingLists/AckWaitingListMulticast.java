package project.Communication.AckWaitingLists;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TimerTask;
import java.util.UUID;

import project.CLI.CLI;
import project.Communication.Messages.Message;
import project.Communication.Sender;
import project.Model.Peer;

/**
 * This class is an {@link AckWaitingList} specialized in handling acks for multicast messages.
 */
public class AckWaitingListMulticast extends AckWaitingList{

    private final Set<Peer> ackingPeers;
    private final Message messageToResend;

    /**
     * Builds an instance of {@link AckWaitingListMulticast}.
     *
     * @param ackID the UUID of the ack.
     * @param sender a reference to the {@link Sender}.
     * @param peers the set of peers we are expecting an ack from.
     * @param messageToResend the message to resend in case an ack was not received.
     */
    public AckWaitingListMulticast(UUID ackID, Sender sender, Set<Peer> peers, Message messageToResend) {
        super(ackID, sender, 
            new TimerTask() {
                @Override
                public void run() {
                    try {
                        sender.sendMessage(messageToResend);
                    } catch (IOException ignored) {}
                }
            }
        );
        this.messageToResend = messageToResend;
        this.ackingPeers = new HashSet<>();
        this.ackingPeers.addAll(peers);
    }

    /**
     * Updates the AckWaitingList upon reception of the acknowledgement from a peer.
     *
     * @param peer the peer that has acknowledged the reception of the message.
     */
    public void update(Peer peer) {

        if (peer == null) {
            return;
        }   

        ackingPeers.removeIf(p -> p.getIdentifier().toString().equals(peer.getIdentifier().toString()));

        if (ackingPeers.isEmpty()) {
            timer.cancel();
            CLI.printDebug("acks received successfully, stopping timer!");
            completed = true;
        }
    }

    /**
     * Stops the scheduled action and marks the AckWaitingList as resolved.
     */
    public void onRoomDeletion() {
        timer.cancel();
        CLI.printDebug("The room was canceled, so the acks are no more needed! Stopping timer!");
        completed = true;
    }

    /**
     * Returns the peers that still need to acknowledge the reception of a message.
     *
     * @return the peers that still need to acknowledge the reception of a message
     */
    public Set<Peer> getAckingPeers() {
        return ackingPeers;
    }

    /**
     * Returns the message to resend in case some acks are missing.
     *
     * @return the message to resend in case some acks are missing.
     */
    public Message getMessageToResend() {
        return messageToResend;
    }

}
