package project.Exceptions;

public class PeerAlreadyPresentException extends Exception{

    private String message;

    public PeerAlreadyPresentException(String message){
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
