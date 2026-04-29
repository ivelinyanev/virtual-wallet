package example.backend.mappers;

import example.backend.dtos.transaction.AdminTransactionResponse;
import example.backend.dtos.transaction.UserTransactionResponse;
import example.backend.enums.TransactionType;
import example.backend.models.Transaction;
import example.backend.models.Wallet;

public class TransactionMapper {

    public static UserTransactionResponse toUserTransactionResponse(Transaction tx) {
        // For TRANSFER_IN the record stores sender as `wallet` and recipient as
        // `counterpartyWallet`. Swap perspective so `userWallet` is always the
        // querying user's wallet and `otherWallet` is always the counterparty.
        boolean incoming = tx.getType() == TransactionType.TRANSFER_IN;

        Wallet userWallet  = incoming ? tx.getCounterpartyWallet() : tx.getWallet();
        Wallet otherWallet = incoming ? tx.getWallet()             : tx.getCounterpartyWallet();

        return new UserTransactionResponse(
                tx.getId(),
                tx.getAmount(),
                tx.getCurrency(),
                tx.getType(),
                tx.getStatus(),
                tx.getTimestamp(),
                userWallet  != null ? userWallet.getName()                  : null,
                otherWallet != null ? otherWallet.getOwner().getUsername()   : null
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
