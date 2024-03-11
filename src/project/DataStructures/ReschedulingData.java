package project.DataStructures;

import project.Communication.Messages.Message;

public class ReschedulingData {

    private final Message message;
    private int numOfTries;

    public ReschedulingData(Message message){
        this.message = message;
        this.numOfTries = 0;
    }

    public Message getMessage(){
        return message;
    }

    public int getNumOfTries(){
        return numOfTries;
    }

    public void reschedule(){
        this.numOfTries++;
    }

}
