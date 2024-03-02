package project.Rooms;

import project.Peer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreatedRoom extends Room{

    private Map<UUID, Boolean> acksReceived;

    public CreatedRoom(String name){
        super(UUID.randomUUID().toString(), name);
        acksReceived = new HashMap<>();
    }

    public void confirmAck(UUID uuid){
        acksReceived.put(uuid, true);
    }

}
