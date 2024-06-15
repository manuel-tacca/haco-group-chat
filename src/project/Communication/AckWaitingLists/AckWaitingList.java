package project.Communication.AckWaitingLists;

import project.Communication.Sender;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * This class
 */
public abstract class AckWaitingList {

    protected final UUID ackID;
    protected final Sender sender;
    protected final Timer timer;
    protected final TimerTask action;
    protected boolean isComplete;
    protected static final long DELAY = 5000;
    
    public AckWaitingList(UUID ackID, Sender sender, TimerTask action){
        this.ackID = ackID;
        this.sender = sender;
        this.timer = new Timer();
        this.action = action;
        this.isComplete = false;
    }
    
    public UUID getAckID() {
        return ackID;
    }

    public Boolean getIsComplete() {
        return isComplete;
    }

    public void startTimer() {
        timer.scheduleAtFixedRate(action, DELAY, DELAY);
    }

    //TODO: public abstract void update(); ??
}
