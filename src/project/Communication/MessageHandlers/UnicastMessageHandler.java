package project.Communication.MessageHandlers;

import project.Client;
import project.Communication.Messages.Message;
import project.Communication.Messages.PingMessage;
import project.Communication.Messages.PongMessage;
import project.Communication.Messages.RoomMembershipMessage;

public class UnicastMessageHandler extends MessageHandler {

    public UnicastMessageHandler(Client client) {
        super(client);
    }

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
