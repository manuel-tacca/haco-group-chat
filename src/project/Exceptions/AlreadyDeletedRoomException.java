package project.Exceptions;

public class AlreadyDeletedRoomException extends Exception{

    private final String message;

    public AlreadyDeletedRoomException(String message){
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}