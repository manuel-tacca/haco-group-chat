package project.Communication;

import project.CLI.CLI;
import project.Communication.Messages.Message;
import project.DataStructures.ReschedulingData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static project.Communication.NetworkUtils.UNICAST_PORT_NUMBER;
import static project.Communication.NetworkUtils.MULTICAST_PORT_NUMBER;

public class Sender{

    private final DatagramSocket socket;
    private final ScheduledExecutorService executor;
    private final List<Message> pendingMessages;
    private final List<ReschedulingData> fairScheduler; // key: numOfTries, value: sequenceNumber

    public Sender(){
        try {
            this.socket = new DatagramSocket();
            this.socket.setBroadcast(true);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        pendingMessages = new ArrayList<>();
        fairScheduler = new ArrayList<>();
        executor = Executors.newSingleThreadScheduledExecutor();
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

    private void stopSendingPendingPacketsAtFixedRate(){
        executor.shutdownNow();
    }

    public void sendPacket(Message message) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(message);
        byte[] data = baos.toByteArray();
        socket.send(new DatagramPacket(data, data.length, message.getDestinationAddress(), message.getDestinationPort()));
        CLI.printDebug("SENT: " + message.getType() + "(length: " + data.length + ")" + "\nTO: " + message.getDestinationAddress());
    }

    public void removePendingMessage(Message message){
        pendingMessages.remove(message);
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

    public void close(){
        stopSendingPendingPacketsAtFixedRate();
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

}
