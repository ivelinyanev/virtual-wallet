package example.backend.services;

import example.backend.models.Transaction;
import example.backend.repositories.TransactionRepository;
import example.backend.services.implementations.TransactionServiceImpl;
import example.backend.utils.AuthUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTests {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AuthUtils authUtils;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    void getAllTransactions_Should_ReturnAllTransactions() {
        List<Transaction> transactions = List.of(new Transaction(), new Transaction());

        when(transactionRepository.findAll()).thenReturn(transactions);

        List<Transaction> result = transactionService.getAllTransactions();

        assertEquals(result.size(), transactions.size());
    }

    
}
