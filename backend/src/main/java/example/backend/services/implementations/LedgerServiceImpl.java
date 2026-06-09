package example.backend.services.implementations;

import example.backend.enums.TransactionStatus;
import example.backend.enums.TransactionType;
import example.backend.models.Card;
import example.backend.models.Transaction;
import example.backend.models.Wallet;
import example.backend.repositories.TransactionRepository;
import example.backend.services.protocols.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.transaction.annotation.Propagation.MANDATORY;

@Service
@RequiredArgsConstructor
public class LedgerServiceImpl implements LedgerService {

    private final TransactionRepository transactionRepository;

    @Override
    @Transactional(propagation = MANDATORY)
    public void recordTransfer(Wallet from, Wallet to, BigDecimal debitAmount, BigDecimal creditAmount) {
        save(from, to, debitAmount.negate(), TransactionType.TRANSFER_OUT, null);
        save(to, from, creditAmount, TransactionType.TRANSFER_IN, null);
    }

    @Override
    @Transactional(propagation = MANDATORY)
    public void recordTopUp(Wallet wallet, Card card, BigDecimal amount) {
        save(wallet, null, amount, TransactionType.TOP_UP, card);
    }

    @Override
    @Transactional(propagation = MANDATORY)
    public void recordWithdrawal(Wallet wallet, Card card, BigDecimal amount) {
        save(wallet, null, amount.negate(), TransactionType.WITHDRAWAL, card);
    }

    private void save(Wallet wallet, Wallet counterparty, BigDecimal amount, TransactionType type, Card card) {
        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setCounterpartyWallet(counterparty);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setStatus(TransactionStatus.SUCCESSFUL);
        transaction.setCurrency(wallet.getCurrency());
        transaction.setCard(card);

        transactionRepository.save(transaction);
    }
}
