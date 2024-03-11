package project.Model;

import project.Exceptions.PeerAlreadyPresentException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreatedRoom extends Room{

    public CreatedRoom(String name){
        super(UUID.randomUUID().toString(), name, 0);
    }

    @Override
    public void addPeer(Peer newPeer) throws PeerAlreadyPresentException {
        for (Peer peer: otherRoomMembers){
            if (peer.getIdentifier() == newPeer.getIdentifier()){
                throw new PeerAlreadyPresentException("Peer (" + newPeer.getIdentifier() + ", " + newPeer.getUsername() + ") is already a member of the " + name + " room.");
            }
        }
        otherRoomMembers.add(newPeer);
        setMembersNumber(getMembersNumber()+1);
    }

}
