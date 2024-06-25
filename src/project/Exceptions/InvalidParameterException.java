package project.Exceptions;

/**
 * This exception is thrown when a wrong or unexpected parameter is given.
 */
public class InvalidParameterException extends Exception{

    private final String message;

    /**
     * This exception is thrown when an invalid parameter is passed to a method.
     * @param message the message of the exception.
     */
    public InvalidParameterException(String message){
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
