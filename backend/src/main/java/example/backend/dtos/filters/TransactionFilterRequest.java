package example.backend.dtos.filters;

import example.backend.enums.TransactionStatus;
import example.backend.enums.TransactionType;

import java.time.LocalDateTime;

public record TransactionFilterRequest(
        Long targetUserId,
        Long transactionId,
        TransactionStatus status,
        TransactionType type,
        LocalDateTime from,
        LocalDateTime to
) {
    public static TransactionFilterRequest empty() {
        return new TransactionFilterRequest(null, null, null, null, null, null);
    }
}
