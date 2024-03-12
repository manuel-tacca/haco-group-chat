package project;

import project.Communication.MulticastListener;

public class RoomHandler {
    private MulticastListener listener;

    public RoomHandler(MulticastListener listener) {
        this.listener = listener;
        Thread roomThread = new Thread(listener);
        roomThread.start();
    }
}
