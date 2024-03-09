package project.Exceptions;

import project.Model.CreatedRoom;

import java.util.List;

public class SameRoomNameException extends Exception {
    private String message;

    private List<CreatedRoom> filteredRooms;

    public SameRoomNameException(String message, List<CreatedRoom> filteredRooms){
        this.message = message;
        this.filteredRooms = filteredRooms;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public List<CreatedRoom> getFilteredRooms() { return filteredRooms; }
}
