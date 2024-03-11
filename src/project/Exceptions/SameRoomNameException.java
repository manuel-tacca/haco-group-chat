package project.Exceptions;

import project.Model.CreatedRoom;
import project.Model.Room;

import java.util.List;

public class SameRoomNameException extends Exception {
    private String message;

    private List<Room> filteredRooms;

    public SameRoomNameException(String message, List<Room> filteredRooms){
        this.message = message;
        this.filteredRooms = filteredRooms;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public List<Room> getFilteredRooms() { return filteredRooms; }
}
