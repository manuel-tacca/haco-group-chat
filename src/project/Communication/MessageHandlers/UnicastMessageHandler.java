package project.Communication.MessageHandlers;

import project.Client;
import project.Communication.Messages.*;

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
                client.handleRoomMembership(roomMembershipMessage.getRoom(), roomMembershipMessage.getAckID(), roomMembershipMessage.getSourceAddress());
                break;
            case LEAVE_NETWORK:
                LeaveNetworkMessage leaveNetworkMessage = (LeaveNetworkMessage) message;
                client.handleLeaveNetwork(leaveNetworkMessage.getPeer(), leaveNetworkMessage.getAckID());
                break;
            case ACK_ROOM_MEMBERSHIP:
                AckRoomMembershipMessage ackRoomMembershipMessage = (AckRoomMembershipMessage) message;
                client.handleUnicastAck(ackRoomMembershipMessage.getAckID(), ackRoomMembershipMessage.getSourceAddress());
            case ACK_LEAVE_NETWORK:
                AckLeaveNetworkMessage ackLeaveNetworkMessage = (AckLeaveNetworkMessage) message;
                client.handleMulticastAck(ackLeaveNetworkMessage.getAckID());
            default:
                break;
        }
    }

}
