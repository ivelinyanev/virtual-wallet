package example.backend.exceptions;

public class ImpossibleOperationException extends RuntimeException {
    public ImpossibleOperationException(String message) {
        super(message);
    }
}
