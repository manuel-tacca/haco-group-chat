package project.Communication.Listeners;

import java.net.*;

import project.Model.Room;
import project.Communication.MessageHandlers.MulticastMessageHandler;

public class MulticastListener extends Listener{

    private final Room room;

    public MulticastListener(MulticastSocket multicastSocket, MulticastMessageHandler multicastMessageHandler, Room room){
        super(multicastSocket, multicastMessageHandler);
        this.room = room;
    }

    public Room getRoom() {
        return room;
    }
    
}
