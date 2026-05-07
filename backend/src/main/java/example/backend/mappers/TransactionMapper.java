package example.backend.mappers;

import example.backend.dtos.transaction.AdminTransactionResponse;
import example.backend.dtos.transaction.UserTransactionResponse;
import example.backend.models.Transaction;

public class TransactionMapper {

    public static UserTransactionResponse toUserTransactionResponse(Transaction tx) {
        return new UserTransactionResponse(
                tx.getId(),
                tx.getAmount(),
                tx.getCurrency(),
                tx.getType(),
                tx.getStatus(),
                tx.getTimestamp(),
                tx.getWallet() != null ? tx.getWallet().getName() : null,
                tx.getCounterpartyWallet() != null ? tx.getCounterpartyWallet().getOwner().getUsername() : null
        );
    }

    public static AdminTransactionResponse toAdminTransactionResponse(Transaction tx) {
        return new AdminTransactionResponse(
                tx.getId(),
                tx.getAmount(),
                tx.getCurrency(),
                tx.getType(),
                tx.getStatus(),
                tx.getTimestamp(),
                tx.getWallet() != null ? tx.getWallet().getName() : null,
                tx.getWallet() != null ? tx.getWallet().getOwner().getUsername() : null,
                tx.getCounterpartyWallet() != null ? tx.getCounterpartyWallet().getName() : null,
                tx.getCounterpartyWallet() != null ? tx.getCounterpartyWallet().getOwner().getUsername() : null
        );
    }
}
