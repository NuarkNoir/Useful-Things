package xyz.nuark.trashbox.exceptions;

public class AuthException extends Exception {
    String message;

    public AuthException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
    
}
