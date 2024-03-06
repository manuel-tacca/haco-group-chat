package project.Messages;

import java.net.InetAddress;

public record Message(byte[] content, InetAddress destinationAddress) {

    public int getLength() {
        return this.content.length;
    }

}
