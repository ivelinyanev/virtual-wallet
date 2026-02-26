package example.backend.services;

import example.backend.dtos.filters.TransactionFilterRequest;
import example.backend.models.Transaction;
import example.backend.models.User;
import example.backend.models.Wallet;
import example.backend.repositories.TransactionRepository;
import example.backend.services.implementations.TransactionServiceImpl;
import example.backend.utils.AuthUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
        Transaction t1 = new Transaction();
        Transaction t2 = new Transaction();
        List<Transaction> transactions = List.of(t1, t2);

        Page<Transaction> transactionPage = new PageImpl<>(transactions);

        when(transactionRepository.findAll(
                any(Specification.class),
                any(Pageable.class))
        ).thenReturn(transactionPage);

        TransactionFilterRequest request = new TransactionFilterRequest(
                null, null, null, null, null, null
        );

        Page<Transaction> result = transactionService.getAllTransactions(
                request,
                Pageable.unpaged()
        );

        assertEquals(2, result.getContent().size());
    }

    @Test
    void getMyTransactions_Should_ReturnMyTransactions() {
        User owner = new User();
        owner.setUsername("owner");

        when(authUtils.getAuthenticatedUser()).thenReturn(owner);

        Wallet wallet = new Wallet();
        wallet.setOwner(owner);

        Transaction t1 = new Transaction();
        Transaction t2 = new Transaction();

        t1.setWallet(wallet);
        t2.setCounterpartyWallet(wallet);

        List<Transaction> transactions = List.of(t1, t2);
        Page<Transaction> transactionPage = new PageImpl<>(transactions);

        when(transactionRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(transactionPage);

        TransactionFilterRequest request = new TransactionFilterRequest(
                null, null, null, null, null, null
        );

        Page<Transaction> result = transactionService.getMyTransactions(
                request, Pageable.unpaged()
        );

        assertEquals(2, result.getContent().size());
    }

}
