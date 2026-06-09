package example.backend.services.protocols;

import example.backend.models.Card;
import example.backend.models.Wallet;

import java.math.BigDecimal;

public interface LedgerService {

    void recordTransfer(Wallet from, Wallet to, BigDecimal debitAmount, BigDecimal creditAmount);

    void recordTopUp(Wallet wallet, Card card, BigDecimal amount);

    void recordWithdrawal(Wallet wallet, Card card, BigDecimal amount);
}
