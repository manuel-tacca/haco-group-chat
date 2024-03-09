package project.DataStructures;

public class ReschedulingData {

    private final int sequenceNumber;
    private int numOfTries;

    public ReschedulingData(int sequenceNumber){
        this.sequenceNumber = sequenceNumber;
        this.numOfTries = 0;
    }

    public int getSequenceNumber(){
        return sequenceNumber;
    }

    public int getNumOfTries(){
        return numOfTries;
    }

    public void reschedule(){
        this.numOfTries++;
    }

}
