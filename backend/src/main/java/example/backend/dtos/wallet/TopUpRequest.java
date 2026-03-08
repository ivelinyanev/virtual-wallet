package example.backend.dtos.wallet;

import java.math.BigDecimal;

public record TopUpRequest(
        Long walletId,
        Long cardId,
        BigDecimal amount
) {
}
