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

public class AckWaitingListMulticast extends AckWaitingList{

    private final Set<Peer> ackingPeers;

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
        this.ackingPeers = new HashSet<>();
        this.ackingPeers.addAll(peers);
    }
    
    public void update(Peer peer) {

        if (peer == null) {
            return;
        }   

        ackingPeers.removeIf(p -> p.getIdentifier().toString().equals(peer.getIdentifier().toString()));

        if (ackingPeers.isEmpty()) {
            timer.cancel();
            CLI.printDebug("acks received successfully, stopping timer!");
            isComplete = true;
        }
    }

    public Set<Peer> getAckingPeers() {
        return ackingPeers;
    }

}
