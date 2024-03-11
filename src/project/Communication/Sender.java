package project.Communication;

import project.CLI.CLI;
import project.Client;
import project.Communication.Messages.Message;
import project.Communication.Messages.MessageType;
import project.DataStructures.ReschedulingData;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Sender{

    public static final int PORT_NUMBER = 9999;
    private final Client client;
    private final DatagramSocket socket;
    private final ScheduledExecutorService executor;
    private final List<Message> pendingMessages;
    private final List<ReschedulingData> fairScheduler; // key: numOfTries, value: sequenceNumber

    public Sender(Client client){
        try {
            this.socket = new DatagramSocket();
            this.socket.setBroadcast(true);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        pendingMessages = new ArrayList<>();
        fairScheduler = new ArrayList<>();
        executor = Executors.newSingleThreadScheduledExecutor();
        this.client = client;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public void sendPendingPacketsAtFixedRate(int rateSeconds){
        executor.scheduleAtFixedRate(() -> {
            if(!pendingMessages.isEmpty()){
                updateScheduler();
                try {
                    sendPacket(chooseMessageToResend());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 0, rateSeconds, TimeUnit.SECONDS);
    }

    public void stopSendingPendingPacketsAtFixedRate(){
        executor.shutdownNow();
    }

    public void sendPacket(Message message) throws IOException {
        DatagramPacket responsePacket = new DatagramPacket(message.content(), message.getLength(), message.destinationAddress(), PORT_NUMBER);
        socket.send(responsePacket);
        CLI.printDebug("SENT: " + message.getHumanReadableContent() + ", TO: " + message.destinationAddress());
    }

    public void removePendingMessage(UUID processUUID, int sequenceNumber){
        pendingMessages.removeIf(message -> message.destinationProcessID().equals(processUUID));
    }

    private void updateScheduler(){
        for (Message message: pendingMessages){
            if(fairScheduler.stream().noneMatch(x -> x.getMessage() == message)){
                fairScheduler.add(new ReschedulingData(message));
            }
        }
    }

    private Message chooseMessageToResend(){
        ReschedulingData reschedulingData = fairScheduler.get(0);
        for(ReschedulingData data: fairScheduler){
            if (reschedulingData == data){
                continue;
            }
            if(data.getNumOfTries() < reschedulingData.getNumOfTries()){
                reschedulingData = data;
            }
        }
        reschedulingData.reschedule();
        return reschedulingData.getMessage();
    }

}
