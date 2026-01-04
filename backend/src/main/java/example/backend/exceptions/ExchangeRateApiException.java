package example.backend.exceptions;

public class ExchangeRateApiException extends RuntimeException {
    public ExchangeRateApiException(String message) {
        super(message);
    }
}
