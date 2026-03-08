package example.backend.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.stream.Collectors;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationException(
            AuthorizationException ex,
            HttpServletRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
                403,
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN) // 403
                .body(error);
    }

    @ExceptionHandler(AccountNotVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotVerifiedException(
            AccountNotVerifiedException ex,
            HttpServletRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
                403,
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN) // 403
                .body(error);
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateException(
            DuplicateException ex,
            HttpServletRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
                409,
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT) // 409
                .body(error);
    }

    @ExceptionHandler(EmailException.class)
    public ResponseEntity<ErrorResponse> handleEmailException(
            EmailException ex,
            HttpServletRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
                400,
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 400
                .body(error);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(
            EntityNotFoundException ex,
            HttpServletRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
                404,
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND) // 404
                .body(error);
    }

    @ExceptionHandler(ExchangeRateApiException.class)
    public ResponseEntity<ErrorResponse> handleExchangeRateApiException(
            ExchangeRateApiException ex,
            HttpServletRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
                503,
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE) // 503
                .body(error);
    }

    @ExceptionHandler(ImpossibleOperationException.class)
    public ResponseEntity<ErrorResponse> handleImpossibleOperationException(
            ImpossibleOperationException ex,
            HttpServletRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
                400,
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 400
                .body(error);
    }

    @ExceptionHandler(InvalidCardException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCardException(
            InvalidCardException ex,
            HttpServletRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
                400,
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 400
                .body(error);
    }

    @ExceptionHandler(UserBlockedException.class)
    public ResponseEntity<ErrorResponse> handleUserBlockedException(
            UserBlockedException ex,
            HttpServletRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
                403,
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN) // 403
                .body(error);
    }

    @ExceptionHandler(UserNotVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleUserNotVerifiedException(
            UserNotVerifiedException ex,
            HttpServletRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
                403,
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN) // 403
                .body(error);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
                400,
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(
            BindException ex,
            HttpServletRequest request
    ) {
        Map<String, String> errors = ex.getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (a, b) -> a
                ));

        ErrorResponse error = new ErrorResponse(
                400,
                "Invalid request parameters",
                request.getRequestURI(),
                errors
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {

        ErrorResponse error = new ErrorResponse(
                400,
                "Invalid value for parameter '" + ex.getName() + "'",
                request.getRequestURI(),
                Map.of(
                        "parameter", ex.getName(),
                        "value", String.valueOf(ex.getValue()),
                        "expectedType", ex.getRequiredType().getSimpleName()
                )
        );

        return ResponseEntity.badRequest().body(error);
    }
}
