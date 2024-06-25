package project.Exceptions;

import project.Model.Room;

import java.util.List;

/**
 * This exception is thrown when a user is trying to enter or delete a group chat that shares the name with
 * another one.
 */
public class SameRoomNameException extends Exception {
    
    private final String message;
    private final List<Room> filteredRooms;

    /**
     * This exception is thrown when two rooms with the same name are present in the same list.
     * @param message The message of the exception.
     * @param filteredRooms The list of rooms.
     */
    public SameRoomNameException(String message, List<Room> filteredRooms){
        this.message = message;
        this.filteredRooms = filteredRooms;
    }

    /**
     * Returns the message of the exception.
     *
     * @return the message of the exception.
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * This method returns the list of rooms with the same name.
     *
     * @return The list of rooms with the same name.
     */
    public List<Room> getFilteredRooms() { return filteredRooms; }
}
