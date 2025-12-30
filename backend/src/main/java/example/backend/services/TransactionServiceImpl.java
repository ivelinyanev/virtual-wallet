package example.backend.services;

import example.backend.exceptions.EntityNotFoundException;
import example.backend.exceptions.ImpossibleOperationException;
import example.backend.models.Transaction;
import example.backend.models.User;
import example.backend.repositories.TransactionRepository;
import example.backend.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static example.backend.utils.StringConstants.YOU_DO_NOT_OWN_THAT_TRANSACTION;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AuthUtils authUtils;

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('USER')")
    public List<Transaction> getMyTransactions(Long id) {
        User actingUser = authUtils.getAuthenticatedUser();

        return transactionRepository.findAllByWallet_Owner(actingUser);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('USER')")
    public Transaction getMyTransactionById(Long id) {
        User actingUser = authUtils.getAuthenticatedUser();

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaction", "id", String.valueOf(id)));

        if (!transaction.getWallet().getOwner().equals(actingUser)) {
            throw new ImpossibleOperationException(YOU_DO_NOT_OWN_THAT_TRANSACTION);
        }

        return transaction;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Transaction getAnyTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaction", "id", String.valueOf(id)));
    }

    @Override
    public Transaction createTransaction(Transaction transaction) {
        return null;
    }

}
