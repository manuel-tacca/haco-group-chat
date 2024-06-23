package project.Exceptions;

public class PeerAlreadyPresentException extends Exception{

    private final String message;

    /**
     * This exception is thrown when a peer is already present in a specific list
     * @param message
     */
    public PeerAlreadyPresentException(String message){
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
