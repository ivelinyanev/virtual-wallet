package example.backend.services;

import example.backend.dtos.wallet.TopUpRequest;
import example.backend.dtos.wallet.WithdrawRequest;
import example.backend.enums.Currency;
import example.backend.enums.TransactionType;
import example.backend.exceptions.AuthorizationException;
import example.backend.exceptions.EntityNotFoundException;
import example.backend.exceptions.ImpossibleOperationException;
import example.backend.models.Card;
import example.backend.models.User;
import example.backend.models.Wallet;
import example.backend.repositories.CardRepository;
import example.backend.repositories.WalletRepository;
import example.backend.services.implementations.WalletServiceImpl;
import example.backend.services.protocols.LedgerService;
import example.backend.services.protocols.PaymentService;
import example.backend.utils.AuthUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static example.backend.utils.StringConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class WalletServiceTests {

    @Mock private WalletRepository walletRepository;
    @Mock private AuthUtils authUtils;
    @Mock private PaymentService paymentService;
    @Mock private LedgerService ledger;
    @Mock private CardRepository cardRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    private User owner;
    private Wallet wallet;
    private Card card;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setUsername("owner");

        wallet = new Wallet();
        wallet.setId(10L);
        wallet.setOwner(owner);
        wallet.setCurrency(Currency.EUR);
        wallet.setBalance(BigDecimal.ZERO);

        card = new Card();
        card.setId(5L);
        card.setToken("tok_test");

        when(authUtils.getAuthenticatedUser()).thenReturn(owner);
    }

    // ───────────────────────── getMyWallets ─────────────────────────

    @Test
    void getMyWallets_Should_ReturnOwnersWallets() {
        when(walletRepository.findAllByOwnerAndDeletedFalse(owner)).thenReturn(List.of(wallet));

        List<Wallet> result = walletService.getMyWallets();

        assertEquals(1, result.size());
        assertEquals(wallet, result.get(0));
    }

    // ───────────────────────── getWalletById ─────────────────────────

    @Test
    void getWalletById_Should_ReturnWallet_When_OwnerRequests() {
        when(walletRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(wallet));

        Wallet result = walletService.getWalletById(10L);

        assertEquals(wallet, result);
    }

    @Test
    void getWalletById_Should_Throw_When_NotFound() {
        when(walletRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> walletService.getWalletById(99L));
    }

    @Test
    void getWalletById_Should_Throw_When_NotOwner() {
        User other = new User();
        other.setUsername("other");
        wallet.setOwner(other);

        when(walletRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(wallet));

        assertThrows(AuthorizationException.class, () -> walletService.getWalletById(10L));
    }

    // ───────────────────────── createWallet ─────────────────────────

    @Test
    void createWallet_Should_SaveWallet_With_SpecifiedCurrency() {
        Wallet newWallet = new Wallet();
        newWallet.setCurrency(Currency.USD);

        when(walletRepository.existsByCurrencyAndOwnerAndDeletedFalse(Currency.USD, owner)).thenReturn(false);
        when(walletRepository.save(newWallet)).thenReturn(newWallet);

        Wallet result = walletService.createWallet(newWallet);

        assertEquals(Currency.USD, result.getCurrency());
        assertEquals(owner, result.getOwner());
    }

    @Test
    void createWallet_Should_DefaultToEUR_When_CurrencyIsNull() {
        Wallet newWallet = new Wallet();
        // currency left null

        when(walletRepository.existsByCurrencyAndOwnerAndDeletedFalse(Currency.EUR, owner)).thenReturn(false);
        when(walletRepository.save(newWallet)).thenReturn(newWallet);

        walletService.createWallet(newWallet);

        assertEquals(Currency.EUR, newWallet.getCurrency());
    }

    @Test
    void createWallet_Should_Throw_When_DuplicateCurrencyForOwner() {
        Wallet newWallet = new Wallet();
        newWallet.setCurrency(Currency.EUR);

        when(walletRepository.existsByCurrencyAndOwnerAndDeletedFalse(Currency.EUR, owner)).thenReturn(true);

        ImpossibleOperationException ex =
                assertThrows(ImpossibleOperationException.class,
                        () -> walletService.createWallet(newWallet));

        assertTrue(ex.getMessage().contains("EUR"));
        verify(walletRepository, never()).save(any());
    }

    // ───────────────────────── createBaseWalletUponVerification ─────────────────────────

    @Test
    void createBaseWalletUponVerification_Should_SaveEURWallet_ForUser() {
        walletService.createBaseWalletUponVerification(owner);

        verify(walletRepository).save(argThat(w ->
                w.getOwner().equals(owner) &&
                w.getCurrency() == Currency.EUR
        ));
    }

    // ───────────────────────── deleteWallet ─────────────────────────

    @Test
    void deleteWallet_Should_SoftDelete_When_Valid() {
        Wallet second = new Wallet();
        second.setId(20L);
        second.setOwner(owner);
        second.setBalance(BigDecimal.ZERO);

        when(walletRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(wallet));
        when(walletRepository.findAllByOwnerAndDeletedFalse(owner)).thenReturn(List.of(wallet, second));

        walletService.deleteWallet(10L);

        assertTrue(wallet.isDeleted());
        verify(walletRepository).save(wallet);
        verify(walletRepository, never()).delete(any());
    }

    @Test
    void deleteWallet_Should_Throw_When_NotFound() {
        when(walletRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> walletService.deleteWallet(99L));
    }

    @Test
    void deleteWallet_Should_Throw_When_NotOwner() {
        User other = new User();
        other.setUsername("other");
        wallet.setOwner(other);

        when(walletRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(wallet));

        assertThrows(AuthorizationException.class, () -> walletService.deleteWallet(10L));
        verify(walletRepository, never()).delete(any());
        verify(walletRepository, never()).save(any());
    }

    @Test
    void deleteWallet_Should_Throw_When_LastWallet() {
        when(walletRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(wallet));
        when(walletRepository.findAllByOwnerAndDeletedFalse(owner)).thenReturn(List.of(wallet)); // only one

        ImpossibleOperationException ex =
                assertThrows(ImpossibleOperationException.class,
                        () -> walletService.deleteWallet(10L));

        assertEquals(CANNOT_DELETE_LAST_WALLET, ex.getMessage());
        verify(walletRepository, never()).delete(any());
        verify(walletRepository, never()).save(any());
    }

    @Test
    void deleteWallet_Should_Throw_When_BalanceIsPositive() {
        wallet.setBalance(new BigDecimal("50"));

        when(walletRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(wallet));

        ImpossibleOperationException ex =
                assertThrows(ImpossibleOperationException.class,
                        () -> walletService.deleteWallet(10L));

        assertEquals(CANNOT_DELETE_WALLET_WITH_POSITIVE_BALANCE, ex.getMessage());
        verify(walletRepository, never()).delete(any());
        verify(walletRepository, never()).save(any());
    }

    // ───────────────────────── topUp ─────────────────────────

    @Test
    void topUp_Should_IncreasBalance_And_RecordTransaction_When_Valid() {
        wallet.setBalance(new BigDecimal("100"));
        TopUpRequest req = new TopUpRequest(10L, 5L, new BigDecimal("50"));

        when(walletRepository.findByIdForUpdate(10L)).thenReturn(wallet);
        when(cardRepository.findById(5L)).thenReturn(Optional.of(card));

        walletService.topUp(req);

        assertEquals(new BigDecimal("150"), wallet.getBalance());
        verify(paymentService).charge("tok_test", new BigDecimal("50"));
        verify(walletRepository).save(wallet);
        verify(ledger).recordTopUp(wallet, card, new BigDecimal("50"));
    }

    @Test
    void topUp_Should_Throw_When_WalletNotFound() {
        TopUpRequest req = new TopUpRequest(99L, 5L, new BigDecimal("50"));

        when(walletRepository.findByIdForUpdate(99L)).thenReturn(null);
        when(cardRepository.findById(5L)).thenReturn(Optional.of(card));

        assertThrows(EntityNotFoundException.class, () -> walletService.topUp(req));
    }

    @Test
    void topUp_Should_Throw_When_CardNotFound() {
        TopUpRequest req = new TopUpRequest(10L, 99L, new BigDecimal("50"));

        when(walletRepository.findByIdForUpdate(10L)).thenReturn(wallet);
        // cardRepository.findById(99L) returns Optional.empty() by default

        assertThrows(EntityNotFoundException.class, () -> walletService.topUp(req));
    }

    @Test
    void topUp_Should_Throw_When_NotOwner() {
        User other = new User();
        other.setUsername("other");
        wallet.setOwner(other);

        TopUpRequest req = new TopUpRequest(10L, 5L, new BigDecimal("50"));

        when(walletRepository.findByIdForUpdate(10L)).thenReturn(wallet);
        when(cardRepository.findById(5L)).thenReturn(Optional.of(card));

        assertThrows(AuthorizationException.class, () -> walletService.topUp(req));
        verify(paymentService, never()).charge(any(), any());
    }

    @Test
    void topUp_Should_Throw_When_AmountIsZero() {
        TopUpRequest req = new TopUpRequest(10L, 5L, BigDecimal.ZERO);

        when(walletRepository.findByIdForUpdate(10L)).thenReturn(wallet);
        when(cardRepository.findById(5L)).thenReturn(Optional.of(card));

        ImpossibleOperationException ex =
                assertThrows(ImpossibleOperationException.class, () -> walletService.topUp(req));

        assertEquals(TOP_UP_AMOUNT_MUST_BE_POSITIVE, ex.getMessage());
        verify(paymentService, never()).charge(any(), any());
    }

    @Test
    void topUp_Should_Throw_When_AmountIsNegative() {
        TopUpRequest req = new TopUpRequest(10L, 5L, new BigDecimal("-1"));

        when(walletRepository.findByIdForUpdate(10L)).thenReturn(wallet);
        when(cardRepository.findById(5L)).thenReturn(Optional.of(card));

        assertThrows(ImpossibleOperationException.class, () -> walletService.topUp(req));
        verify(paymentService, never()).charge(any(), any());
    }

    // ───────────────────────── withdraw ─────────────────────────

    @Test
    void withdraw_Should_DecreaseBalance_And_RecordTransaction_When_Valid() {
        wallet.setBalance(new BigDecimal("200"));
        WithdrawRequest req = new WithdrawRequest(10L, 5L, new BigDecimal("80"));

        when(walletRepository.findByIdForUpdate(10L)).thenReturn(wallet);
        when(cardRepository.findById(5L)).thenReturn(Optional.of(card));

        walletService.withdraw(req);

        assertEquals(new BigDecimal("120"), wallet.getBalance());
        verify(paymentService).withdraw("tok_test", new BigDecimal("80"));
        verify(walletRepository).save(wallet);
        verify(ledger).recordWithdrawal(wallet, card, new BigDecimal("80"));
    }

    @Test
    void withdraw_Should_Throw_When_WalletNotFound() {
        WithdrawRequest req = new WithdrawRequest(99L, 5L, new BigDecimal("50"));

        when(walletRepository.findByIdForUpdate(99L)).thenReturn(null);
        when(cardRepository.findById(5L)).thenReturn(Optional.of(card));

        assertThrows(EntityNotFoundException.class, () -> walletService.withdraw(req));
    }

    @Test
    void withdraw_Should_Throw_When_CardNotFound() {
        wallet.setBalance(new BigDecimal("100"));
        WithdrawRequest req = new WithdrawRequest(10L, 99L, new BigDecimal("50"));

        when(walletRepository.findByIdForUpdate(10L)).thenReturn(wallet);
        // cardRepository.findById(99L) returns Optional.empty() by default

        assertThrows(EntityNotFoundException.class, () -> walletService.withdraw(req));
    }

    @Test
    void withdraw_Should_Throw_When_NotOwner() {
        User other = new User();
        other.setUsername("other");
        wallet.setOwner(other);
        wallet.setBalance(new BigDecimal("100"));

        WithdrawRequest req = new WithdrawRequest(10L, 5L, new BigDecimal("50"));

        when(walletRepository.findByIdForUpdate(10L)).thenReturn(wallet);
        when(cardRepository.findById(5L)).thenReturn(Optional.of(card));

        assertThrows(AuthorizationException.class, () -> walletService.withdraw(req));
        verify(paymentService, never()).withdraw(any(), any());
    }

    @Test
    void withdraw_Should_Throw_When_AmountIsZero() {
        wallet.setBalance(new BigDecimal("100"));
        WithdrawRequest req = new WithdrawRequest(10L, 5L, BigDecimal.ZERO);

        when(walletRepository.findByIdForUpdate(10L)).thenReturn(wallet);
        when(cardRepository.findById(5L)).thenReturn(Optional.of(card));

        ImpossibleOperationException ex =
                assertThrows(ImpossibleOperationException.class, () -> walletService.withdraw(req));

        assertEquals(WITHDRAWAL_AMOUNT_MUST_BE_POSITIVE, ex.getMessage());
        verify(paymentService, never()).withdraw(any(), any());
    }

    @Test
    void withdraw_Should_Throw_When_InsufficientFunds() {
        wallet.setBalance(new BigDecimal("30"));
        WithdrawRequest req = new WithdrawRequest(10L, 5L, new BigDecimal("100"));

        when(walletRepository.findByIdForUpdate(10L)).thenReturn(wallet);
        when(cardRepository.findById(5L)).thenReturn(Optional.of(card));

        ImpossibleOperationException ex =
                assertThrows(ImpossibleOperationException.class, () -> walletService.withdraw(req));

        assertEquals(INSUFFICIENT_FUNDS, ex.getMessage());
        verify(paymentService, never()).withdraw(any(), any());
    }
}
