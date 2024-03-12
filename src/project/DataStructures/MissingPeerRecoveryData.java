package project.DataStructures;

public class MissingPeerRecoveryData {

    private String peerID;
    private String roomID;

    public MissingPeerRecoveryData(String peerID, String roomID){
        this.peerID = peerID;
        this.roomID = roomID;
    }

    public String getPeerID() {
        return peerID;
    }

    public String getRoomID() {
        return roomID;
    }
}
