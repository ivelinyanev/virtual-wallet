package example.backend.exceptions;

public class InvalidCardException extends RuntimeException {
    public InvalidCardException(String message) {
        super(message);
    }
}
