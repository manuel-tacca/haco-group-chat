package project.Communication.Listeners;

import java.net.*;

import project.Communication.MessageHandlers.UnicastMessageHandler;

public class UnicastListener extends Listener{

    public UnicastListener(DatagramSocket socket, UnicastMessageHandler unicastMessageHandler) {
        super(socket, unicastMessageHandler);
    }
}
