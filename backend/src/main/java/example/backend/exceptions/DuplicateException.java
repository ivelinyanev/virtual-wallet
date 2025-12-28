package example.backend.exceptions;

public class DuplicateException extends RuntimeException {
    public DuplicateException(String message) {
        super(message);
    }

    public DuplicateException(String entity, String attribute, String value) {
        super(String.format("%s with %s %s already exists!", entity, attribute, value));
    }
}
