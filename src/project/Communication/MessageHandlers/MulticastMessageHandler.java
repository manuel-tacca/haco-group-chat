package project.Communication.MessageHandlers;

import project.Client;
import project.Communication.Messages.AckDeleteRoomMessage;
import project.Communication.Messages.AckRoomTextMessage;
import project.Communication.Messages.DeleteRoomMessage;
import project.Communication.Messages.Message;
import project.Communication.Messages.RoomTextMessage;

/**
 * This class is a {@link MessageHandler} specialized in handling multicast messages.
 */
public class MulticastMessageHandler extends MessageHandler {

    /**
     * Builds an instance of {@link MulticastMessageHandler}.
     *
     * @param client The controller of the application.
     */
    public MulticastMessageHandler(Client client){
        super(client);
    }

    /**
     * Handles a multicast message based on its type.
     *
     * @param message The multicast message to handle.
     * @throws Exception If something wrong happens.
     */
    @Override
    public void handle(Message message) throws Exception{
        super.handle(message);
        switch (message.getType()) {
            case ROOM_TEXT:
                RoomTextMessage roomTextMessage = (RoomTextMessage) message;
                client.handleRoomText(roomTextMessage.getRoomText(), roomTextMessage.getAckID());
                break;
            case DELETE_ROOM:
                DeleteRoomMessage deleteRoomMessage = (DeleteRoomMessage) message;
                client.handleDeleteRoom(deleteRoomMessage.getRoomUUID(), deleteRoomMessage.getAckID());
                break;
            case ACK_DELETE_ROOM:
                AckDeleteRoomMessage ackDeleteRoomMessage = (AckDeleteRoomMessage) message;
                client.handleMulticastAck(ackDeleteRoomMessage.getAckID());
            case ACK_ROOM_TEXT:
                AckRoomTextMessage ackRoomTextMessage = (AckRoomTextMessage) message;
                client.handleMulticastAck(ackRoomTextMessage.getAckID());
            default:
                break;
        }
    }

}
