package project.Communication;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import project.CLI.CLI;

public abstract class AckWaitingList {

    private UUID ackWaitingListID;
    private Timer timer;
    private TimerTask action;
    private long delay;
    private Sender sender;

    public AckWaitingList(UUID ackWaitingListID, Sender sender) {
        this.ackWaitingListID = ackWaitingListID;
        this.sender = sender;
        this.timer = new Timer();
        this.delay = 1000;
        initializeAction();
    }

    protected void initializeAction() {
        this.action = new TimerTask() {
            public void run() { onTimerExpired(); }
        };
    }

    protected abstract void onTimerExpired();

    public void startTimer() {
        timer.schedule(action, delay);
    }

    public UUID getAckWaitingListID() {
        return ackWaitingListID; 
    }
}
