package example.backend.services;

import example.backend.dtos.transfer.TransferReq;
import example.backend.enums.Currency;
import example.backend.exceptions.AccountNotVerifiedException;
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

import java.math.BigDecimal;
import java.util.Optional;

import static example.backend.utils.StringConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransferServiceTests {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionServiceImpl transactionService;

    @Mock
    private ConversionService conversionService;

    @Mock
    private UserServiceImpl userService;

    @Mock
    private AuthUtils authUtils;

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
        fromUser.setVerified(true);

        toUser = new User();
        toUser.setId(2L);
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
        // verify transfer service is called twice !
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
    void transfer_Should_ConvertCurrency_When_Needed() {
        TransferReq req = new TransferReq(10L, "toUser", BigDecimal.TEN);

        fromWallet.setCurrency(Currency.USD);
        toWallet.setCurrency(Currency.EUR);

        when(walletRepository.findByIdForUpdate(10L)).thenReturn(fromWallet);
        when(userService.getByUsername("toUser")).thenReturn(toUser);
        when(walletRepository.findByOwnerAndCurrency(toUser, Currency.USD))
                .thenReturn(java.util.Optional.empty());
        when(walletRepository.findByOwnerAndCurrency(toUser, Currency.EUR))
                .thenReturn(java.util.Optional.of(toWallet));
        when(conversionService.convert(Currency.USD, Currency.EUR, BigDecimal.TEN))
                .thenReturn(new BigDecimal("9"));

        transferService.transfer(req);

        assertEquals(new BigDecimal("990"), fromWallet.getBalance());
        assertEquals(new BigDecimal("509"), toWallet.getBalance());
    }
}
