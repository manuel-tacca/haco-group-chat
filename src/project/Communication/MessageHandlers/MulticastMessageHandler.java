package project.Communication.MessageHandlers;

import project.Client;
import project.Communication.Messages.DeleteRoomMessage;
import project.Communication.Messages.Message;
import project.Communication.Messages.RoomTextMessage;

public class MulticastMessageHandler extends MessageHandler {

    public MulticastMessageHandler(Client client){
        super(client);
    }

    @Override
    public void handle(Message message) throws Exception{
        super.handle(message);
        switch (message.getType()) {
            case ROOM_TEXT:
                RoomTextMessage roomTextMessage = (RoomTextMessage) message;
                client.handleRoomText(roomTextMessage.getRoomText());
                break;
            case DELETE_ROOM:
                DeleteRoomMessage deleteRoomMessage = (DeleteRoomMessage) message;
                client.handleDeleteRoom(deleteRoomMessage.getRoomUUID());
                break;
            default:
                break;
        }
    }

}
