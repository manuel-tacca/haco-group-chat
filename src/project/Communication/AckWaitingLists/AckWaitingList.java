package project.Communication.AckWaitingLists;

import project.Communication.Sender;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * This abstract class is to be used as a base for specialized AckWaitingLists. AckWaitingLists can be specialized in
 * a particular type of communication (e.g. {@link AckWaitingListUnicast}, {@link AckWaitingListMulticast}). The role
 * of an {@link AckWaitingList} is to store the information needed to understand whether a message was successfully
 * received by all the addressees, and to schedule the re-sending of messages that we are not sure that have been
 * received.
 */
public abstract class AckWaitingList {

    protected final UUID ackID;
    protected final Sender sender;
    protected final Timer timer;
    protected final TimerTask action;
    protected boolean completed;
    protected static final long DELAY = 5000;

    /**
     * Sets the parameters that are common to every {@link AckWaitingList}.
     *
     * @param ackID the UUID of the ack.
     * @param sender a reference to the {@link Sender}.
     * @param action the action to perform every DELAY milliseconds.
     */
    public AckWaitingList(UUID ackID, Sender sender, TimerTask action){
        this.ackID = ackID;
        this.sender = sender;
        this.timer = new Timer();
        this.action = action;
        this.completed = false;
    }

    /**
     * Returns the ack UUID of the waiting list.
     *
     * @return the ack UUID of the waiting list.
     */
    public UUID getAckID() {
        return ackID;
    }

    /**
     * Returns whether all the acks related to the ackID have been received.
     *
     * @return whether all the acks related to the ackID have been received.
     */
    public boolean isComplete() {
        return completed;
    }

    /**
     * Schedules the given action at a fixed rate of DELAY milliseconds, starting DELAY milliseconds from the
     * scheduling time.
     */
    public void startTimer() {
        timer.scheduleAtFixedRate(action, DELAY, DELAY);
    }

}
