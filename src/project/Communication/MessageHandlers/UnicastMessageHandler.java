package project.Communication.MessageHandlers;

import project.Client;
import project.Communication.Messages.Message;
import project.Communication.Messages.PingMessage;
import project.Communication.Messages.PongMessage;
import project.Communication.Messages.RoomMembershipMessage;

/**
 * This class is a {@link MessageHandler} specialized in handling unicast and broadcast messages.
 */
public class UnicastMessageHandler extends MessageHandler {

    /**
     * Builds an instance of {@link UnicastMessageHandler}.
     *
     * @param client The controller of the application.
     */
    public UnicastMessageHandler(Client client) {
        super(client);
    }

    /**
     * Handles a unicast or a broadcast message based on its type.
     *
     * @param message The unicast or broadcast message to handle.
     * @throws Exception If something wrong happens.
     */
    @Override
    public void handle(Message message) throws Exception {
        super.handle(message);
        switch(message.getType()){
            case PING:
                PingMessage pingMessage = (PingMessage) message;
                client.handlePing(pingMessage.getPeer());
                break;
            case PONG:
                PongMessage pongMessage = (PongMessage) message;
                client.handlePong(pongMessage.getPeer());
                break;
            case ROOM_MEMBERSHIP:
                RoomMembershipMessage roomMembershipMessage = (RoomMembershipMessage) message;
                client.handleRoomMembership(roomMembershipMessage.getRoom());
                break;
            default:
                break;
        }
    }

}
