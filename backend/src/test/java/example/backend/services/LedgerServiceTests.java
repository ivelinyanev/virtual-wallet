package example.backend.services;

import example.backend.enums.Currency;
import example.backend.enums.TransactionStatus;
import example.backend.enums.TransactionType;
import example.backend.models.Card;
import example.backend.models.Transaction;
import example.backend.models.Wallet;
import example.backend.repositories.TransactionRepository;
import example.backend.services.implementations.LedgerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LedgerServiceTests {

    @Mock private TransactionRepository transactionRepository;

    @InjectMocks
    private LedgerServiceImpl ledger;

    private Wallet fromWallet;
    private Wallet toWallet;
    private Card card;

    @BeforeEach
    void setUp() {
        fromWallet = new Wallet();
        fromWallet.setId(1L);
        fromWallet.setCurrency(Currency.EUR);

        toWallet = new Wallet();
        toWallet.setId(2L);
        toWallet.setCurrency(Currency.USD);

        card = new Card();
        card.setId(10L);
        card.setToken("tok_test");
    }

    @Test
    void recordTransfer_Should_SaveTwoTransactions_WithCorrectFields() {
        BigDecimal debit = new BigDecimal("100");
        BigDecimal credit = new BigDecimal("108");

        ledger.recordTransfer(fromWallet, toWallet, debit, credit);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(2)).save(captor.capture());

        List<Transaction> saved = captor.getAllValues();

        Transaction out = saved.get(0);
        assertEquals(TransactionType.TRANSFER_OUT, out.getType());
        assertEquals(new BigDecimal("-100"), out.getAmount());
        assertEquals(fromWallet, out.getWallet());
        assertEquals(toWallet, out.getCounterpartyWallet());
        assertEquals(Currency.EUR, out.getCurrency());
        assertEquals(TransactionStatus.SUCCESSFUL, out.getStatus());
        assertNull(out.getCard());

        Transaction in = saved.get(1);
        assertEquals(TransactionType.TRANSFER_IN, in.getType());
        assertEquals(credit, in.getAmount());
        assertEquals(toWallet, in.getWallet());
        assertEquals(fromWallet, in.getCounterpartyWallet());
        assertEquals(Currency.USD, in.getCurrency());
        assertEquals(TransactionStatus.SUCCESSFUL, in.getStatus());
        assertNull(in.getCard());
    }

    @Test
    void recordTopUp_Should_SaveOneTransaction_WithCorrectFields() {
        BigDecimal amount = new BigDecimal("50");

        ledger.recordTopUp(fromWallet, card, amount);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(1)).save(captor.capture());

        Transaction saved = captor.getValue();
        assertEquals(TransactionType.TOP_UP, saved.getType());
        assertEquals(amount, saved.getAmount());
        assertEquals(fromWallet, saved.getWallet());
        assertNull(saved.getCounterpartyWallet());
        assertEquals(card, saved.getCard());
        assertEquals(Currency.EUR, saved.getCurrency());
        assertEquals(TransactionStatus.SUCCESSFUL, saved.getStatus());
    }

    @Test
    void recordWithdrawal_Should_SaveOneTransaction_WithNegatedAmount_AndCorrectFields() {
        BigDecimal amount = new BigDecimal("75");

        ledger.recordWithdrawal(fromWallet, card, amount);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(1)).save(captor.capture());

        Transaction saved = captor.getValue();
        assertEquals(TransactionType.WITHDRAWAL, saved.getType());
        assertEquals(new BigDecimal("-75"), saved.getAmount());
        assertEquals(fromWallet, saved.getWallet());
        assertNull(saved.getCounterpartyWallet());
        assertEquals(card, saved.getCard());
        assertEquals(Currency.EUR, saved.getCurrency());
        assertEquals(TransactionStatus.SUCCESSFUL, saved.getStatus());
    }
}
