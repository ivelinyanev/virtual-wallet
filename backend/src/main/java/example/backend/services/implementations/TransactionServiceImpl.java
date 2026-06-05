package example.backend.services.implementations;

import example.backend.annotations.RequiresVerifiedAccount;
import example.backend.dtos.filters.TransactionFilterRequest;
import example.backend.enums.TransactionStatus;
import example.backend.enums.TransactionType;
import example.backend.exceptions.EntityNotFoundException;
import example.backend.exceptions.ImpossibleOperationException;
import example.backend.models.Card;
import example.backend.models.Transaction;
import example.backend.models.User;
import example.backend.models.Wallet;
import example.backend.repositories.TransactionRepository;
import example.backend.services.protocols.TransactionService;
import example.backend.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static example.backend.specifications.TransactionSpecifications.buildSpec;
import static example.backend.utils.StringConstants.YOU_DO_NOT_OWN_THAT_TRANSACTION;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AuthUtils authUtils;

    @Override
    @Transactional(readOnly = true)
    @RequiresVerifiedAccount
    @PreAuthorize("hasRole('ADMIN')")
    public Page<Transaction> getAllTransactions(TransactionFilterRequest request, Pageable pageable) {

        Specification<Transaction> spec = buildSpec(null, request);

        return transactionRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    @RequiresVerifiedAccount
    @PreAuthorize("hasRole('USER')")
    public Page<Transaction> getMyTransactions(TransactionFilterRequest request, Pageable pageable) {
        User actingUser = authUtils.getAuthenticatedUser();

        Specification<Transaction> spec = buildSpec(actingUser, request);

        return transactionRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    @RequiresVerifiedAccount
    @PreAuthorize("hasRole('USER')")
    public Transaction getMyTransactionById(Long id) {
        User actingUser = authUtils.getAuthenticatedUser();

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaction", "id", String.valueOf(id)));

        if (transaction.getWallet() == null || !transaction.getWallet().getOwner().equals(actingUser)) {
            throw new ImpossibleOperationException(YOU_DO_NOT_OWN_THAT_TRANSACTION);
        }

        return transaction;
    }

    @Override
    @Transactional(readOnly = true)
    @RequiresVerifiedAccount
    @PreAuthorize("hasRole('ADMIN')")
    public Transaction getAnyTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaction", "id", String.valueOf(id)));
    }

    @Transactional
    void recordTransaction(
            Wallet wallet,
            Wallet counterparty,
            BigDecimal amount,
            TransactionType type
    ) {
        recordTransaction(wallet, counterparty, amount, type, null);
    }

    @Transactional
    void recordTransaction(
            Wallet wallet,
            Wallet counterparty,
            BigDecimal amount,
            TransactionType type,
            Card card
    ) {
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
