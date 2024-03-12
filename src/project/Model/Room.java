package project.Model;

import project.Exceptions.PeerAlreadyPresentException;

import java.io.PrintStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Room {
    protected UUID identifier;
    protected String name;
    protected List<Peer> otherRoomMembers; // all the member but the client itself!
    protected int membersNumber;
    protected InetAddress multicastAddress;
    protected int multicastPort;

    protected final PrintStream out = System.out;

    public Room(String uuid, String name, int membersNumber){
        this.identifier = UUID.fromString(uuid);
        this.name = name;
        this.membersNumber = membersNumber;
        this.multicastAddress = multicastAddress;
        this.multicastPort = multicastPort;
        this.otherRoomMembers = new ArrayList<>();
    }

    public void addPeer(Peer newPeer) throws PeerAlreadyPresentException{
        for (Peer peer: otherRoomMembers){
            if (peer.getIdentifier() == newPeer.getIdentifier()){
                throw new PeerAlreadyPresentException("Peer (" + newPeer.getIdentifier() + ", " + newPeer.getUsername() + ") is already a member of the " + name + " room.");
            }
        }
        otherRoomMembers.add(newPeer);
    }

    public String getName() {
        return name;
    }

    public List<Peer> getOtherRoomMembers() {
        return otherRoomMembers;
    }

    public UUID getIdentifier(){
        return identifier;
    }

    public int getMembersNumber() { return membersNumber; }

    public void setMembersNumber(int membersNumber) { this.membersNumber = membersNumber; }

    public Integer getMulticastPort() {
        return this.multicastPort;
    }

    public InetAddress getMulticastAddress() {
        return this.multicastAddress;
    }
}
