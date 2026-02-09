package example.backend.dtos.wallet;

import example.backend.enums.Currency;

import java.math.BigDecimal;

public record PrivateWalletDto(
        Long id,
        String ownerName,
        String walletName,
        BigDecimal balance,
        Currency currency
) {
}
