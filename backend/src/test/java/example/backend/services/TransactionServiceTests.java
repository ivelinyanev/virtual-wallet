package example.backend.services;

import example.backend.dtos.filters.TransactionFilterRequest;
import example.backend.exceptions.EntityNotFoundException;
import example.backend.exceptions.ImpossibleOperationException;
import example.backend.models.Transaction;
import example.backend.models.User;
import example.backend.models.Wallet;
import example.backend.repositories.TransactionRepository;
import example.backend.services.implementations.TransactionServiceImpl;
import example.backend.services.protocols.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Optional;

import static example.backend.utils.StringConstants.YOU_DO_NOT_OWN_THAT_TRANSACTION;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTests {

    @Mock private TransactionRepository transactionRepository;
    @Mock private AuthorizationService authorizationService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User owner;
    private Wallet wallet;
    private TransactionFilterRequest emptyFilter;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setUsername("owner");

        wallet = new Wallet();
        wallet.setOwner(owner);

        emptyFilter = new TransactionFilterRequest(null, null, null, null, null, null);
    }

    // ───────────────────────── getAllTransactions ─────────────────────────

    @Test
    void getAllTransactions_Should_ReturnAllTransactions() {
        Page<Transaction> page = new PageImpl<>(List.of(new Transaction(), new Transaction()));

        when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        Page<Transaction> result = transactionService.getAllTransactions(emptyFilter, Pageable.unpaged());

        assertEquals(2, result.getContent().size());
    }

    // ───────────────────────── getMyTransactions ─────────────────────────

    @Test
    void getMyTransactions_Should_ReturnTransactionsForAuthenticatedUser() {
        when(authorizationService.currentUser()).thenReturn(owner);

        Transaction t1 = new Transaction();
        Transaction t2 = new Transaction();
        t1.setWallet(wallet);
        t2.setCounterpartyWallet(wallet);

        Page<Transaction> page = new PageImpl<>(List.of(t1, t2));

        when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        Page<Transaction> result = transactionService.getMyTransactions(emptyFilter, Pageable.unpaged());

        assertEquals(2, result.getContent().size());
    }

    // ───────────────────────── getMyTransactionById ─────────────────────────

    @Test
    void getMyTransactionById_Should_ReturnTransaction_When_OwnerRequests() {
        when(authorizationService.currentUser()).thenReturn(owner);

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setWallet(wallet);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        Transaction result = transactionService.getMyTransactionById(1L);

        assertEquals(transaction, result);
    }

    @Test
    void getMyTransactionById_Should_Throw_When_NotFound() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> transactionService.getMyTransactionById(99L));
    }

    @Test
    void getMyTransactionById_Should_Throw_When_UserIsNotOwner() {
        User otherUser = new User();
        otherUser.setUsername("other");

        when(authorizationService.currentUser()).thenReturn(otherUser);

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setWallet(wallet);  // wallet is owned by "owner", not "other"

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        ImpossibleOperationException ex =
                assertThrows(ImpossibleOperationException.class,
                        () -> transactionService.getMyTransactionById(1L));

        assertEquals(YOU_DO_NOT_OWN_THAT_TRANSACTION, ex.getMessage());
    }

    // ───────────────────────── getAnyTransactionById ─────────────────────────

    @Test
    void getAnyTransactionById_Should_ReturnTransaction_When_Found() {
        Transaction transaction = new Transaction();
        transaction.setId(1L);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        Transaction result = transactionService.getAnyTransactionById(1L);

        assertEquals(transaction, result);
    }

    @Test
    void getAnyTransactionById_Should_Throw_When_NotFound() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> transactionService.getAnyTransactionById(99L));
    }
}
