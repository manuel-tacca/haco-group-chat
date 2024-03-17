package project.Communication.MessageHandlers;

import project.Client;
import project.Communication.Messages.Message;

import java.net.InetAddress;

public abstract class MessageHandler {

    protected final Client client;

    public MessageHandler(Client client) {
        this.client = client;
    }

    public InetAddress getClientIpAddress(){
        return client.getPeerData().getIpAddress();
    }

    public void handle(Message message) throws Exception{
        // pings and pongs do not have a vector clock
        if(message.getVectorClock() != null){
            client.updateVectorClock(message.getVectorClock());
        }
    }

}
