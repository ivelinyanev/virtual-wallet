package example.backend.dtos.transaction;

import example.backend.enums.Currency;
import example.backend.enums.TransactionStatus;
import example.backend.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
    Long id,
    BigDecimal amount,
    Currency currency,
    TransactionType type,
    TransactionStatus status,
    LocalDateTime timestamp,

//    Long walletId,
    String walletName,
    String walletOwnerUsername,

//    Long counterpartyWalletId,
    String counterPartyWalletName,
    String counterpartyWalletOwnerUsername
) {
}
