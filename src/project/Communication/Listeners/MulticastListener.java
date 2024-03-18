package project.Communication.Listeners;

import java.net.*;

import project.Communication.MessageHandlers.MulticastMessageHandler;
import project.Communication.MessageHandlers.MessageHandler;

/**
 * This class is a {@link Listener} specialized in receiving multicast messages.
 */
public class MulticastListener extends Listener{

    /**
     * Builds an instance of {@link MulticastListener}.
     *
     * @param multicastSocket The multicast socket that will receive the multicast packets.
     * @param multicastMessageHandler The {@link MessageHandler} that will handle the received multicast packets.
     */
    public MulticastListener(MulticastSocket multicastSocket, MulticastMessageHandler multicastMessageHandler){
        super(multicastSocket, multicastMessageHandler);
    }
    
}
