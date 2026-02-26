package example.backend.services.protocols;

import example.backend.dtos.filters.TransactionFilterRequest;
import example.backend.enums.TransactionStatus;
import example.backend.enums.TransactionType;
import example.backend.models.Transaction;
import example.backend.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {

    Page<Transaction> getAllTransactions(TransactionFilterRequest request, Pageable pageable);

    Page<Transaction> getMyTransactions(TransactionFilterRequest request, Pageable pageable);

    Transaction getMyTransactionById(Long id);

    Transaction getAnyTransactionById(Long id);

}
