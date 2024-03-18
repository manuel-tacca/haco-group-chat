package project.Communication.Listeners;

import java.net.*;

import project.Communication.MessageHandlers.MessageHandler;
import project.Communication.MessageHandlers.UnicastMessageHandler;

/**
 * This class is a {@link Listener} specialized in receiving unicast and broadcast messages.
 */
public class UnicastListener extends Listener{

    /**
     * Builds an instance of {@link MulticastListener}.
     *
     * @param socket The socket that will receive the unicast and broadcast packets.
     * @param unicastMessageHandler The {@link MessageHandler} that will handle the received unicast and broadcast packets.
     */
    public UnicastListener(DatagramSocket socket, UnicastMessageHandler unicastMessageHandler) {
        super(socket, unicastMessageHandler);
    }
}
