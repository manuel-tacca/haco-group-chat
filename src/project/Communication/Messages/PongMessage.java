package project.Communication.Messages;

import project.Model.Peer;

import java.io.Serializable;
import java.net.InetAddress;

public class PongMessage extends Message implements Serializable {

    private final Peer peer;

    public PongMessage(InetAddress destinationAddress, int destinationPort, Peer peer) {
        super(MessageType.PONG, null, destinationAddress, destinationPort);
        this.peer = peer;
    }

    public Peer getPeer() {
        return peer;
    }

}
