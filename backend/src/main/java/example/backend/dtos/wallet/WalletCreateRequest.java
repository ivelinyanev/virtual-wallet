package example.backend.dtos.wallet;

import example.backend.enums.Currency;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;

public record WalletCreateRequest(

        @Size(max = 20)
        @Nullable
        String name,

        @Nullable
        Currency currency
) {
}
