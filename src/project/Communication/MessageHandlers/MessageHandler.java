package project.Communication.MessageHandlers;

import project.Client;
import project.Communication.Messages.Message;

public abstract class MessageHandler {

    protected final Client client;

    public MessageHandler(Client client) {
        this.client = client;
    }

    public void handle(Message message) throws Exception{
        // pings and pongs do not have a vector clock
        if(message.getVectorClock() != null){
            client.updateVectorClock(message.getVectorClock());
        }
    }

}
