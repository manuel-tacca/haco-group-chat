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
    protected List<Peer> roomMembers;
    protected InetAddress multicastAddress;

    public Room(String name, InetAddress multicastAddress){
        this.identifier = UUID.randomUUID();
        this.name = name;
        this.multicastAddress = multicastAddress;
        this.roomMembers = new ArrayList<>();
    }

    public Room(UUID uuid, String name, InetAddress multicastAddress){
        this.identifier = uuid;
        this.name = name;
        this.multicastAddress = multicastAddress;
        this.roomMembers = new ArrayList<>();
    }

    public void addPeer(Peer newPeer) throws PeerAlreadyPresentException{
        for (Peer peer: roomMembers){
            if (peer.getIdentifier() == newPeer.getIdentifier()){
                throw new PeerAlreadyPresentException("Peer (" + newPeer.getIdentifier() + ", " + newPeer.getUsername() + ") is already a member of the " + name + " room.");
            }
        }
        roomMembers.add(newPeer);
    }

    public String getName() {
        return name;
    }

    public List<Peer> getRoomMembers() {
        return roomMembers;
    }

    public UUID getIdentifier(){
        return identifier;
    }

    public InetAddress getMulticastAddress() {
        return this.multicastAddress;
    }
}
