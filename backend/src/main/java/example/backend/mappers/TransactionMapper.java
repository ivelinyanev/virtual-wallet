package example.backend.mappers;

import example.backend.dtos.transaction.TransactionResponse;
import example.backend.models.Transaction;

public class TransactionMapper {

    public static TransactionResponse toTransactionResponse(Transaction tx) {
        return new TransactionResponse(
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
