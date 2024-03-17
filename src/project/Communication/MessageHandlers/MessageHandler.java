package project.Communication.MessageHandlers;

import project.Client;
import project.Communication.Messages.Message;


public abstract class MessageHandler {

    protected final Client client;

    public MessageHandler(Client client) {
        this.client = client;
    }

    public abstract void handle(Message message) throws Exception;

}
