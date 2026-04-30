package example.backend.services.implementations;

import example.backend.annotations.RequiresVerifiedAccount;
import example.backend.dtos.transfer.TransferReq;
import example.backend.enums.Currency;
import example.backend.enums.TransactionType;
import example.backend.exceptions.AccountNotVerifiedException;
import example.backend.exceptions.EntityNotFoundException;
import example.backend.exceptions.ImpossibleOperationException;
import example.backend.models.User;
import example.backend.models.Wallet;
import example.backend.repositories.WalletRepository;
import example.backend.services.protocols.ConversionService;
import example.backend.services.protocols.TransferService;
import example.backend.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static example.backend.utils.StringConstants.*;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final WalletRepository walletRepository;
    private final TransactionServiceImpl transactionService;
    private final ConversionService conversionService;
    private final UserServiceImpl userService;
    private final AuthUtils authUtils;

    @Override
    @Transactional
    @RequiresVerifiedAccount
    @PreAuthorize("hasRole('USER')")
    public void transfer(TransferReq request) {
        // source wallet
        Wallet from = walletRepository.findWalletByName(request.fromWalletName())
                .orElseThrow(() -> new EntityNotFoundException("Wallet", "name", request.fromWalletName()));

        validateWalletOwner(from);

        // recipient wallet
        User toUser = userService.getByUsername(request.toUsername());

        if (!toUser.isVerified()) {
            throw new AccountNotVerifiedException(RECIPIENT_NOT_VERIFIED);
        }

        Wallet toWallet = walletRepository
                .findByOwnerAndCurrency(toUser, from.getCurrency())
                .or(() -> walletRepository.findByOwnerAndCurrency(toUser, Currency.EUR))
                .orElseThrow(() -> new ImpossibleOperationException(RECIPIENT_HAS_NO_SUITABLE_WALLET));

        BigDecimal amount = request.amount();

        validateTransferRequest(from.getId(), toWallet.getId(), amount);
        validateWalletState(from, toWallet, amount);

        BigDecimal creditAmount = convertIfNeeded(from, toWallet, amount);

        debit(from, amount);
        credit(toWallet, creditAmount);

        recordTransaction(from, toWallet, amount, creditAmount);
    }

    private void validateTransferRequest(Long fromId, Long toId, BigDecimal amount) {
        validateDifferentWallets(fromId, toId);
        validatePositiveAmount(amount);
    }

    private void validateWalletState(Wallet from, Wallet to, BigDecimal amount) {
        validateBothWalletsFound(from, to);
        validateSufficientAmount(from, amount);
    }

    private void validateWalletOwner(Wallet from) {
        User user = authUtils.getAuthenticatedUser();

        if (!from.getOwner().equals(user)) {
            throw new ImpossibleOperationException(YOU_ARE_NOT_THE_WALLET_OWNER);
        }
    }

    private void validateDifferentWallets(Long fromId, Long toId) {
        if (fromId.equals(toId)) {
            throw new ImpossibleOperationException(SAME_WALLET_TRANSACTION_IMPOSSIBLE);
        }
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ImpossibleOperationException(TRANSFER_AMOUNT_CANNOT_BE_NEGATIVE);
        }
    }

    private void validateBothWalletsFound(Wallet from, Wallet to) {
        if (from == null || to == null) {
            throw new EntityNotFoundException(WALLET_IDS_NOT_FOUND);
        }
    }

    private void validateSufficientAmount(Wallet from, BigDecimal amount) {
        if (from.getBalance().compareTo(amount) < 0) {
            throw new ImpossibleOperationException(INSUFFICIENT_FUNDS);
        }
    }

    private void debit(Wallet from, BigDecimal amount) {
        from.setBalance(from.getBalance().subtract(amount));
    }

    private void credit(Wallet to, BigDecimal amount) {
        to.setBalance(to.getBalance().add(amount));
    }

    private BigDecimal convertIfNeeded(Wallet from, Wallet to, BigDecimal amount) {
        if (from.getCurrency().equals(to.getCurrency())) {
            return amount;
        }

        return conversionService.convert(
                from.getCurrency(),
                to.getCurrency(),
                amount
        );
    }

    private void recordTransaction(Wallet from, Wallet to, BigDecimal debitAmount, BigDecimal creditAmount) {
        transactionService.recordTransaction(
                from,
                to,
                debitAmount.negate(),
                TransactionType.TRANSFER_OUT
        );

        transactionService.recordTransaction(
                to,
                from,
                creditAmount,
                TransactionType.TRANSFER_IN
        );
    }

}
