package project.DataStructures;

public class MissingPeerRecoveryData {

    private String peerID;
    private String roomID;
    private int ackSequenceNumber;

    public MissingPeerRecoveryData(String peerID, String roomID, int ackSequenceNumber){
        this.peerID = peerID;
        this.roomID = roomID;
        this.ackSequenceNumber = ackSequenceNumber;
    }

    public String getPeerID() {
        return peerID;
    }

    public String getRoomID() {
        return roomID;
    }

    public int getAckSequenceNumber() {
        return ackSequenceNumber;
    }
}
