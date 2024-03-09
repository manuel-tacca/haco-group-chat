package project.Exceptions;

public class InvalidRoomNameException extends Exception {
    private String message;

    public InvalidRoomNameException(String message){
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
