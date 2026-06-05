package example.backend.services;

import example.backend.dtos.transfer.OwnWalletTransferReq;
import example.backend.dtos.transfer.TransferReq;
import example.backend.enums.Currency;
import example.backend.exceptions.AccountNotVerifiedException;
import example.backend.exceptions.EntityNotFoundException;
import example.backend.exceptions.ImpossibleOperationException;
import example.backend.models.User;
import example.backend.models.Wallet;
import example.backend.repositories.WalletRepository;
import example.backend.services.implementations.TransactionServiceImpl;
import example.backend.services.implementations.TransferServiceImpl;
import example.backend.services.implementations.UserServiceImpl;
import example.backend.services.protocols.ConversionService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TransferServiceTests {

    @Mock private WalletRepository walletRepository;
    @Mock private TransactionServiceImpl transactionService;
    @Mock private ConversionService conversionService;
    @Mock private UserServiceImpl userService;
    @Mock private AuthUtils authUtils;

    @InjectMocks
    private TransferServiceImpl transferService;

    User fromUser;
    User toUser;
    Wallet fromWallet;
    Wallet toWallet;

    @BeforeEach
    void setUp() {
        fromUser = new User();
        fromUser.setId(1L);
        fromUser.setUsername("fromUser");
        fromUser.setVerified(true);

        toUser = new User();
        toUser.setId(2L);
        toUser.setUsername("toUser");
        toUser.setVerified(true);

        fromWallet = new Wallet();
        fromWallet.setId(10L);
        fromWallet.setOwner(fromUser);
        fromWallet.setCurrency(Currency.EUR);
        fromWallet.setBalance(new BigDecimal("1000"));

        toWallet = new Wallet();
        toWallet.setId(20L);
        toWallet.setOwner(toUser);
        toWallet.setCurrency(Currency.EUR);
        toWallet.setBalance(new BigDecimal("500"));

        when(authUtils.getAuthenticatedUser()).thenReturn(fromUser);
    }

    // ───────────────────────── transfer (user-to-user) ─────────────────────────

    @Test
    void transfer_Should_Succeed_When_AllValid() {
        TransferReq req = new TransferReq(10L, "toUser", new BigDecimal("100"));

        when(walletRepository.findByIdForUpdate(10L)).thenReturn(fromWallet);
        when(userService.getByUsername("toUser")).thenReturn(toUser);
        when(walletRepository.findByOwnerAndCurrency(toUser, Currency.EUR))
                .thenReturn(Optional.of(toWallet));

        transferService.transfer(req);

        assertEquals(new BigDecimal("900"), fromWallet.getBalance());
        assertEquals(new BigDecimal("600"), toWallet.getBalance());
    }

    @Test
    void transfer_Should_Throw_When_AmountNegative() {
        TransferReq req = new TransferReq(10L, "toUser", new BigDecimal("-1"));

        when(walletRepository.findByIdForUpdate(10L)).thenReturn(fromWallet);
        when(userService.getByUsername("toUser")).thenReturn(toUser);
        when(walletRepository.findByOwnerAndCurrency(toUser, Currency.EUR))
                .thenReturn(Optional.of(toWallet));

        ImpossibleOperationException ex =
                assertThrows(ImpossibleOperationException.class, () -> transferService.transfer(req));

        assertEquals(TRANSFER_AMOUNT_CANNOT_BE_NEGATIVE, ex.getMessage());
    }

    @Test
    void transfer_Should_Throw_When_AmountZero() {
        TransferReq req = new TransferReq(10L, "toUser", BigDecimal.ZERO);

        when(walletRepository.findByIdForUpdate(10L)).thenReturn(fromWallet);
        when(userService.getByUsername("toUser")).thenReturn(toUser);
        when(walletRepository.findByOwnerAndCurrency(toUser, Currency.EUR))
                .thenReturn(Optional.of(toWallet));

        ImpossibleOperationException ex =
                assertThrows(ImpossibleOperationException.class, () -> transferService.transfer(req));

        assertEquals(TRANSFER_AMOUNT_CANNOT_BE_NEGATIVE, ex.getMessage());
    }

    @Test
    void transfer_Should_Throw_When_FromAndToSameWallet() {
        TransferReq req = new TransferReq(10L, "toUser", new BigDecimal("100"));

        Wallet toSameWallet = new Wallet();
        toSameWallet.setId(10L);
        toSameWallet.setOwner(toUser);

        when(walletRepository.findByIdForUpdate(10L)).thenReturn(fromWallet);
        when(userService.getByUsername("toUser")).thenReturn(toUser);
        when(walletRepository.findByOwnerAndCurrency(toUser, Currency.EUR))
                .thenReturn(Optional.of(toSameWallet));

        ImpossibleOperationException ex =
                assertThrows(ImpossibleOperationException.class, () -> transferService.transfer(req));

        assertEquals(SAME_WALLET_TRANSACTION_IMPOSSIBLE, ex.getMessage());
    }

    @Test
    void transfer_Should_Throw_When_InsufficientFunds() {
        TransferReq req = new TransferReq(10L, "toUser", new BigDecimal("2000"));

        when(walletRepository.findByIdForUpdate(10L)).thenReturn(fromWallet);
        when(userService.getByUsername("toUser")).thenReturn(toUser);
        when(walletRepository.findByOwnerAndCurrency(toUser, Currency.EUR))
                .thenReturn(Optional.of(toWallet));

        ImpossibleOperationException ex =
                assertThrows(ImpossibleOperationException.class, () -> transferService.transfer(req));

        assertEquals(INSUFFICIENT_FUNDS, ex.getMessage());
    }

    @Test
    void transfer_Should_Throw_When_RecipientNotVerified() {
        TransferReq req = new TransferReq(10L, "toUser", BigDecimal.TEN);
        toUser.setVerified(false);

        when(walletRepository.findByIdForUpdate(10L)).thenReturn(fromWallet);
        when(userService.getByUsername("toUser")).thenReturn(toUser);

        AccountNotVerifiedException ex =
                assertThrows(AccountNotVerifiedException.class, () -> transferService.transfer(req));

        assertEquals(RECIPIENT_NOT_VERIFIED, ex.getMessage());
    }

    @Test
    void transfer_Should_Throw_When_NotWalletOwner() {
        TransferReq req = new TransferReq(10L, "toUser", BigDecimal.TEN);

        User otherUser = new User();
        otherUser.setUsername("other");
        fromWallet.setOwner(otherUser);

        when(walletRepository.findByIdForUpdate(10L)).thenReturn(fromWallet);

        ImpossibleOperationException ex =
                assertThrows(ImpossibleOperationException.class, () -> transferService.transfer(req));

        assertEquals(YOU_ARE_NOT_THE_WALLET_OWNER, ex.getMessage());
    }

    @Test
    void transfer_Should_Throw_When_RecipientHasNoWallet() {
        TransferReq req = new TransferReq(10L, "toUser", BigDecimal.TEN);

        when(walletRepository.findByIdForUpdate(10L)).thenReturn(fromWallet);
        when(userService.getByUsername("toUser")).thenReturn(toUser);
        when(walletRepository.findByOwnerAndCurrency(toUser, Currency.EUR)).thenReturn(Optional.empty());
        when(walletRepository.findAllByOwner(toUser)).thenReturn(List.of());

        ImpossibleOperationException ex =
                assertThrows(ImpossibleOperationException.class, () -> transferService.transfer(req));

        assertEquals(RECIPIENT_HAS_NO_SUITABLE_WALLET, ex.getMessage());
    }

    @Test
    void transfer_Should_ConvertCurrency_When_CurrenciesDiffer() {
        TransferReq req = new TransferReq(10L, "toUser", BigDecimal.TEN);

        fromWallet.setCurrency(Currency.USD);
        toWallet.setCurrency(Currency.EUR);

        when(walletRepository.findByIdForUpdate(10L)).thenReturn(fromWallet);
        when(userService.getByUsername("toUser")).thenReturn(toUser);
        // source currency is USD — no exact match, fall back to findAllByOwner
        when(walletRepository.findByOwnerAndCurrency(toUser, Currency.USD)).thenReturn(Optional.empty());
        when(walletRepository.findAllByOwner(toUser)).thenReturn(List.of(toWallet));
        when(conversionService.convert(Currency.USD, Currency.EUR, BigDecimal.TEN))
                .thenReturn(new BigDecimal("9"));

        transferService.transfer(req);

        assertEquals(new BigDecimal("990"), fromWallet.getBalance());
        assertEquals(new BigDecimal("509"), toWallet.getBalance());
    }

    // ───────────────────────── internalTransfer ─────────────────────────

    @Test
    void internalTransfer_Should_Succeed_When_SameCurrency() {
        // own second wallet in EUR
        Wallet ownWallet = new Wallet();
        ownWallet.setId(30L);
        ownWallet.setOwner(fromUser);
        ownWallet.setCurrency(Currency.EUR);
        ownWallet.setBalance(new BigDecimal("200"));

        OwnWalletTransferReq req = new OwnWalletTransferReq(10L, 30L, new BigDecimal("100"));

        when(walletRepository.findByIdForUpdate(10L)).thenReturn(fromWallet);
        when(walletRepository.findByIdForUpdate(30L)).thenReturn(ownWallet);

        transferService.internalTransfer(req);

        assertEquals(new BigDecimal("900"), fromWallet.getBalance());
        assertEquals(new BigDecimal("300"), ownWallet.getBalance());
    }

    @Test
    void internalTransfer_Should_ConvertCurrency_When_CurrenciesDiffer() {
        Wallet usdWallet = new Wallet();
        usdWallet.setId(30L);
        usdWallet.setOwner(fromUser);
        usdWallet.setCurrency(Currency.USD);
        usdWallet.setBalance(new BigDecimal("200"));

        OwnWalletTransferReq req = new OwnWalletTransferReq(10L, 30L, new BigDecimal("100"));

        when(walletRepository.findByIdForUpdate(10L)).thenReturn(fromWallet);
        when(walletRepository.findByIdForUpdate(30L)).thenReturn(usdWallet);
        when(conversionService.convert(Currency.EUR, Currency.USD, new BigDecimal("100")))
                .thenReturn(new BigDecimal("108"));

        transferService.internalTransfer(req);

        assertEquals(new BigDecimal("900"), fromWallet.getBalance());
        assertEquals(new BigDecimal("308"), usdWallet.getBalance());
    }

    @Test
    void internalTransfer_Should_Throw_When_SameWallet() {
        OwnWalletTransferReq req = new OwnWalletTransferReq(10L, 10L, BigDecimal.TEN);

        when(walletRepository.findByIdForUpdate(10L)).thenReturn(fromWallet);

        ImpossibleOperationException ex =
                assertThrows(ImpossibleOperationException.class,
                        () -> transferService.internalTransfer(req));

        assertEquals(SAME_WALLET_TRANSACTION_IMPOSSIBLE, ex.getMessage());
    }

    @Test
    void internalTransfer_Should_Throw_When_InsufficientFunds() {
        Wallet ownWallet = new Wallet();
        ownWallet.setId(30L);
        ownWallet.setOwner(fromUser);
        ownWallet.setCurrency(Currency.EUR);
        ownWallet.setBalance(BigDecimal.ZERO);

        OwnWalletTransferReq req = new OwnWalletTransferReq(10L, 30L, new BigDecimal("2000"));

        when(walletRepository.findByIdForUpdate(10L)).thenReturn(fromWallet);
        when(walletRepository.findByIdForUpdate(30L)).thenReturn(ownWallet);

        ImpossibleOperationException ex =
                assertThrows(ImpossibleOperationException.class,
                        () -> transferService.internalTransfer(req));

        assertEquals(INSUFFICIENT_FUNDS, ex.getMessage());
    }

    @Test
    void internalTransfer_Should_Throw_When_FromWalletNotOwnedByUser() {
        Wallet ownWallet = new Wallet();
        ownWallet.setId(30L);
        ownWallet.setOwner(fromUser);
        ownWallet.setCurrency(Currency.EUR);

        User other = new User();
        other.setUsername("other");
        fromWallet.setOwner(other);

        OwnWalletTransferReq req = new OwnWalletTransferReq(10L, 30L, BigDecimal.TEN);

        when(walletRepository.findByIdForUpdate(10L)).thenReturn(fromWallet);
        when(walletRepository.findByIdForUpdate(30L)).thenReturn(ownWallet);

        ImpossibleOperationException ex =
                assertThrows(ImpossibleOperationException.class,
                        () -> transferService.internalTransfer(req));

        assertEquals(YOU_ARE_NOT_THE_WALLET_OWNER, ex.getMessage());
    }

    @Test
    void internalTransfer_Should_Throw_When_ToWalletNotOwnedByUser() {
        Wallet otherWallet = new Wallet();
        otherWallet.setId(30L);
        otherWallet.setOwner(toUser);  // different owner
        otherWallet.setCurrency(Currency.EUR);

        OwnWalletTransferReq req = new OwnWalletTransferReq(10L, 30L, BigDecimal.TEN);

        when(walletRepository.findByIdForUpdate(10L)).thenReturn(fromWallet);
        when(walletRepository.findByIdForUpdate(30L)).thenReturn(otherWallet);

        ImpossibleOperationException ex =
                assertThrows(ImpossibleOperationException.class,
                        () -> transferService.internalTransfer(req));

        assertEquals(YOU_ARE_NOT_THE_WALLET_OWNER, ex.getMessage());
    }

    @Test
    void internalTransfer_Should_Throw_When_FromWalletNotFound() {
        OwnWalletTransferReq req = new OwnWalletTransferReq(99L, 30L, BigDecimal.TEN);
        // findByIdForUpdate(99L) returns null by default

        assertThrows(EntityNotFoundException.class,
                () -> transferService.internalTransfer(req));
    }

    @Test
    void internalTransfer_Should_Throw_When_ToWalletNotFound() {
        OwnWalletTransferReq req = new OwnWalletTransferReq(10L, 99L, BigDecimal.TEN);

        when(walletRepository.findByIdForUpdate(10L)).thenReturn(fromWallet);
        // findByIdForUpdate(99L) returns null by default

        assertThrows(EntityNotFoundException.class,
                () -> transferService.internalTransfer(req));
    }
}
