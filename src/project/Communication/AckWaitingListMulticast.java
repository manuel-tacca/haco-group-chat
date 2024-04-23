package project.Communication;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TimerTask;
import java.util.UUID;

import project.Communication.Messages.Message;
import project.Model.Peer;

public class AckWaitingListMulticast extends AckWaitingList{

    private Set<Peer> ackingPeers;
    private Message messageToResend;

    public AckWaitingListMulticast(UUID ackID, Sender sender, Set<Peer> peers, Message messageToResend) {
        super(ackID, sender, 
            new TimerTask() {
                @Override
                public void run() {
                    try {
                        sender.sendMessage(messageToResend);
                    } catch (IOException e) {
                        // TODO eccezione da gestireee
                        e.printStackTrace();
                    }
                }
            }
        );
        this.messageToResend = messageToResend;
        this.ackingPeers = new HashSet<>();
        this.ackingPeers.addAll(peers);
    }
    
    public void update(Peer peer) {

        if (peer == null) {
            return;
        }   

        if (ackingPeers.contains(peer)) {
            ackingPeers.remove(peer);
        }

        if (ackingPeers.isEmpty()) {
            timer.cancel();
            isComplete = true;
        }
    }

}
