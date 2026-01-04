package example.backend.services.protocols;

import example.backend.models.Transaction;

import java.util.List;

public interface TransactionService {

    List<Transaction> getAllTransactions();

    List<Transaction> getMyTransactions(Long id);

    Transaction getMyTransactionById(Long id);

    Transaction getAnyTransactionById(Long id);

}
