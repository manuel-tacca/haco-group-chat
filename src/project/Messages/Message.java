package project.Messages;

import project.Utils.SocketUtils;

import java.net.DatagramPacket;
import java.net.InetAddress;

public record Message(String type, byte[] content, InetAddress destinationAddress) {

    public int getLength() {
        return this.content.length;
    }

    public String getHumanReadableContent(){
        DatagramPacket packet = new DatagramPacket(content, getLength());
        return new String(packet.getData(), 0, packet.getLength());
    }

}
