package project.Exceptions;

public class InvalidParameterException extends Exception{

    private final String message;

    /**
     * This exception is thrown when an invalid parameter is passed to a method.
     * @param message
     */
    public InvalidParameterException(String message){
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
