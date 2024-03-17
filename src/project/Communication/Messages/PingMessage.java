package project.Communication.Messages;

import project.Model.Peer;

import java.io.Serializable;
import java.net.InetAddress;

public class PingMessage extends Message implements Serializable {

    private final Peer peer;

    public PingMessage(InetAddress destinationAddress, int destinationPort, Peer peer) {
        super(MessageType.PING, null, destinationAddress, destinationPort);
        this.peer = peer;
    }

    public Peer getPeer() {
        return peer;
    }

}
