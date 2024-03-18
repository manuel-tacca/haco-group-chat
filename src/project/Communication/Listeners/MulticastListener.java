package project.Communication.Listeners;

import java.io.IOException;
import java.net.*;

import project.Communication.MessageHandlers.MulticastMessageHandler;
import project.Communication.MessageHandlers.MessageHandler;
import project.Model.Room;

/**
 * This class is a {@link Listener} specialized in receiving multicast messages.
 */
public class MulticastListener extends Listener{

    private final InetSocketAddress inetSocketAddress;
    private final NetworkInterface networkInterface;

    /**
     * Builds an instance of {@link MulticastListener}.
     *
     * @param multicastSocket The multicast socket that will receive the multicast packets.
     * @param multicastMessageHandler The {@link MessageHandler} that will handle the received multicast packets.
     * @param inetSocketAddress The {@link InetSocketAddress} of the multicast group.
     * @param networkInterface The {@link NetworkInterface} that is listening the multicast messages.
     */
    public MulticastListener(MulticastSocket multicastSocket, MulticastMessageHandler multicastMessageHandler, InetSocketAddress inetSocketAddress, NetworkInterface networkInterface){
        super(multicastSocket, multicastMessageHandler);
        this.inetSocketAddress = inetSocketAddress;
        this.networkInterface = networkInterface;
    }

    /**
     * Closes the socket that is listening for multicast packets from the LAN. Before doing so, it leaves the
     * related multicast group.
     *
     * @throws IOException If any sort of I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        socket.leaveGroup(inetSocketAddress, networkInterface);
        super.close();
    }
    
}
