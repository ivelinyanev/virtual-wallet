package example.backend.dtos.wallet;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

import static example.backend.utils.StringConstants.FIELD_CANNOT_BE_BLANK;

public record WithdrawRequest(

        @NotNull(message = FIELD_CANNOT_BE_BLANK)
        Long walletId,

        @NotNull(message = FIELD_CANNOT_BE_BLANK)
        Long cardId,

        @NotNull(message = FIELD_CANNOT_BE_BLANK)
        @Positive
        BigDecimal amount
) {
}
