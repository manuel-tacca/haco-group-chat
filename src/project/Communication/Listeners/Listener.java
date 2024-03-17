package project.Communication.Listeners;

import project.CLI.CLI;
import project.Communication.Messages.Message;
import project.Communication.NetworkUtils;
import project.Communication.MessageHandlers.MessageHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public abstract class Listener implements Runnable{

    protected DatagramSocket socket;
    protected MessageHandler messageHandler;
    protected boolean isActive;

    public Listener(DatagramSocket socket, MessageHandler messageHandler){
        this.socket = socket;
        this.messageHandler = messageHandler;
        this.isActive = true;
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run(){

        byte[] buffer = new byte[NetworkUtils.BUF_DIM]; // Dimensione del buffer per i dati ricevuti
        DatagramPacket receivedPacket;

        while (true) {

            // receive packet
            receivedPacket = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(receivedPacket);
            } catch (IOException e) {
                if (isActive) {
                    throw new RuntimeException(e);
                }
            }

            if(!receivedPacket.getAddress().equals(messageHandler.getClientIpAddress())) {
                try {
                    ByteArrayInputStream bais = new ByteArrayInputStream(receivedPacket.getData());
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    Message message = (Message) ois.readObject();
                    CLI.printDebug("RECEIVED: " + message.getType() + "\nFROM: " + receivedPacket.getAddress());
                    messageHandler.handle(message);
                } catch (Exception e) {
                    e.printStackTrace();
                    //TODO
                }
            }
        }
    }

    public void close(){
        isActive = false;
        if(socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

}
