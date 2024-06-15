package project.Communication.MessageHandlers;

import project.Client;
import project.Communication.Messages.Message;
import project.Communication.Listeners.Listener;

import java.net.InetAddress;

/**
 * This abstract class is to be used as a base for specialized message handlers. Message handlers can be specialized in
 * a particular type of communication (e.g. {@link UnicastMessageHandler}, {@link MulticastMessageHandler}). The role
 * of a {@link MessageHandler} is to unwrap data coming from a {@link Listener} and forward it to the correct {@link
 * Client} method.
 */
public abstract class MessageHandler {

    protected final Client client;

    /**
     * Sets the parameter that is common to every {@link MessageHandler}.
     *
     * @param client The controller of the application.
     */
    public MessageHandler(Client client) {
        this.client = client;
    }

    /**
     * Returns the IP address of the controller.
     *
     * @return The IP address of the controller.
     */
    public InetAddress getClientIpAddress(){
        return client.getPeerData().getIpAddress();
    }

    /**
     * Handles a message based on its type.
     *
     * @param message The message to handle.
     * @throws Exception If something wrong happens.
     */
    public void handle(Message message) throws Exception{
    }
}
