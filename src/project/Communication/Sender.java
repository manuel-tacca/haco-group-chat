package project.Communication;

import project.CLI.CLI;
import project.Client;
import project.Communication.Messages.Message;
import project.DataStructures.ReschedulingData;
import project.DataStructures.Tuple2;

import java.io.IOException;
import java.net.*;
import java.sql.Array;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Sender{

    public static final int PORT_NUMBER = 9999;
    private final Client client;
    private DatagramSocket socket;
    private ScheduledExecutorService executor;
    private Map<Integer, Message> pendingMessages; // key: sequenceNumber
    private List<ReschedulingData> fairScheduler; // key: numOfTries, value: sequenceNumber

    public Sender(Client client){
        this.client = client;
        try {
            this.socket = new DatagramSocket();
            this.socket.setBroadcast(true);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        pendingMessages = new HashMap<>();
        fairScheduler = new ArrayList<>();
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public InetAddress getIPAddress(){
        return socket.getInetAddress();
    }

    public void sendPendingPacketsAtFixedRate(int rateSeconds){
        executor.scheduleAtFixedRate(() -> {
            if(!pendingMessages.isEmpty()){
                updateScheduler();
                try {
                    sendPacket(chooseMessageToResend().first(), chooseMessageToResend().second());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 0, rateSeconds, TimeUnit.SECONDS);
    }

    public void stopSendingPendingPacketsAtFixedRate(){
        executor.shutdownNow();
    }

    public void sendPacket(Message message, Integer sequenceNumber) throws IOException {
        if(sequenceNumber != null && !pendingMessages.containsKey(sequenceNumber)){
            putInPending(sequenceNumber, message);
        }
        DatagramPacket responsePacket = new DatagramPacket(message.content(), message.getLength(), message.destinationAddress(), PORT_NUMBER);
        socket.send(responsePacket);
        CLI.printDebug("SENT: " + message.getHumanReadableContent() + ", TO: " + message.destinationAddress());
    }

    public void acknowledge(int sequenceNumber){
        pendingMessages.remove(sequenceNumber);
    }

    private void putInPending(int sequenceNumber, Message message){
        pendingMessages.put(sequenceNumber, message);
    }

    private void updateScheduler(){
        Set<Integer> keys = pendingMessages.keySet();
        for (Integer key: keys){
            if(fairScheduler.stream().noneMatch(x -> x.getSequenceNumber() == key)){
                fairScheduler.add(new ReschedulingData(key));
            }
        }
    }

    private Tuple2<Message, Integer> chooseMessageToResend(){
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
        int seqNum = reschedulingData.getSequenceNumber();
        return new Tuple2<>(pendingMessages.get(seqNum), seqNum);
    }

}
