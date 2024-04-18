package project.Exceptions;

public class InvalidParameterException extends Exception{

    private final String message;

    public InvalidParameterException(String message){
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
