package example.backend.exceptions;

import java.util.Map;

public record ErrorResponse(
        int status,
        String message,
        String path,
        Map<String, String> details
) {
}
