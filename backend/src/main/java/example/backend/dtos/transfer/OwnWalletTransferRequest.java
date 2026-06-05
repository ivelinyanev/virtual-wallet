package example.backend.dtos.transfer;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

import static example.backend.utils.StringConstants.FIELD_CANNOT_BE_BLANK;

public record OwnWalletTransferRequest(

        @NotNull(message = FIELD_CANNOT_BE_BLANK)
        Long fromWalletId,

        @NotNull(message = FIELD_CANNOT_BE_BLANK)
        Long toWalletId,

        @NotNull(message = FIELD_CANNOT_BE_BLANK)
        @Positive
        BigDecimal amount
) {
}
