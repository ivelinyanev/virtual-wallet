package example.backend.dtos.transfer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

import static example.backend.utils.StringConstants.FIELD_CANNOT_BE_BLANK;

public record TransferReq(

        @NotBlank(message = FIELD_CANNOT_BE_BLANK)
        String fromWalletName,

        @NotBlank(message = FIELD_CANNOT_BE_BLANK)
        String toUsername,

        @NotNull(message = FIELD_CANNOT_BE_BLANK)
        @Positive
        BigDecimal amount
) {
}
