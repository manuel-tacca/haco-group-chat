package project.Exceptions;

/**
 * This exception is thrown when a peer is already present in a collection.
 */
public class PeerAlreadyPresentException extends Exception{

    private final String message;

    /**
     * This exception is thrown when a peer is already present in a specific list
     * @param message the message of the exception.
     */
    public PeerAlreadyPresentException(String message){
        this.message = message;
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
}
