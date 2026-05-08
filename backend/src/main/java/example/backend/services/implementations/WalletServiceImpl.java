package example.backend.services.implementations;

import example.backend.annotations.RequiresVerifiedAccount;
import example.backend.dtos.wallet.TopUpRequest;
import example.backend.enums.Currency;
import example.backend.enums.TransactionType;
import example.backend.exceptions.*;
import example.backend.models.Card;
import example.backend.models.User;
import example.backend.models.Wallet;
import example.backend.repositories.CardRepository;
import example.backend.repositories.WalletRepository;
import example.backend.services.protocols.CardService;
import example.backend.services.protocols.PaymentService;
import example.backend.services.protocols.TransactionService;
import example.backend.services.protocols.WalletService;
import example.backend.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static example.backend.utils.StringConstants.*;

@RequiredArgsConstructor
@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final AuthUtils authUtils;
    private final PaymentService paymentService;
    private final TransactionServiceImpl transactionService;
    private final CardRepository cardRepository;

    @Override
    @Transactional(readOnly = true)
    @RequiresVerifiedAccount
    @PreAuthorize("hasRole('USER')")
    public List<Wallet> getMyWallets() {
        User actingUser = authUtils.getAuthenticatedUser();

        return walletRepository.findAllByOwner(actingUser);
    }

    @Override
    @Transactional(readOnly = true)
    @RequiresVerifiedAccount
    @PreAuthorize("hasRole('USER')")
    public Wallet getWalletById(Long id) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Wallet", "id", String.valueOf(id)));

        if (!isOwner(wallet)) {
            throw new AuthorizationException(CANNOT_ACCESS_WALLET_YOU_ARE_NOT_OWNER_OF);
        }

        return wallet;
    }

    @Override
    @Transactional
    @RequiresVerifiedAccount
    @PreAuthorize("hasRole('USER')")
    public Wallet createWallet(Wallet wallet) {
        User actingUser = authUtils.getAuthenticatedUser();

        wallet.setOwner(actingUser);

        /*
            If currency is not specified, set to EUR by default
         */
        if (wallet.getCurrency() == null) {
            wallet.setCurrency(Currency.EUR);
        }

        enforceOneWalletPerCurrency(wallet.getCurrency(), wallet.getOwner());

        return walletRepository.save(wallet);
    }

    @Override
    @Transactional
    public void createBaseWalletUponVerification(User user) {
        Wallet wallet = new Wallet();

        wallet.setOwner(user);
        wallet.setCurrency(Currency.EUR);
        wallet.setName("Your EUR Wallet");

        walletRepository.save(wallet);
    }

    @Override
    @Transactional
    @RequiresVerifiedAccount
    @PreAuthorize("hasRole('USER')")
    public void deleteWallet(Long id) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Wallet", "id", String.valueOf(id)));

        if (wallet.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new ImpossibleOperationException(CANNOT_DELETE_WALLET_WITH_POSITIVE_BALANCE);
        }
        if (!isOwner(wallet)) {
            throw new AuthorizationException(CANNOT_DELETE_WALLET_YOU_ARE_NOT_OWNER_OF);
        }

        stopWalletDeletionIfLastWallet(wallet);
        stopWalletDeletionIfBalancePositive(wallet);

        walletRepository.delete(wallet);
    }

    @Override
    @Transactional
    @RequiresVerifiedAccount
    @PreAuthorize("hasRole('USER')")
    public void topUp(TopUpRequest request) {
        Long walletId = request.walletId();
        Long cardId = request.cardId();
        BigDecimal amount = request.amount();

        Wallet wallet = walletRepository.findByIdForUpdate(walletId);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card", "id", String.valueOf(cardId)));

        if (wallet == null) {
            throw new EntityNotFoundException("Wallet", "id", String.valueOf(walletId));
        }

        if (!isOwner(wallet)) {
            throw new AuthorizationException(CANNOT_ACCESS_WALLET_YOU_ARE_NOT_OWNER_OF);
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ImpossibleOperationException(TOP_UP_AMOUNT_MUST_BE_POSITIVE);
        }

        // try catch block for top up simulation should catch failed top-ups
        // for now no fail conditions have been added
        paymentService.charge(card.getToken(), amount);

        wallet.setBalance(wallet.getBalance().add(amount));

        walletRepository.save(wallet);

         transactionService.recordTransaction(
                wallet,
                null,
                amount,
                TransactionType.TOP_UP
        );
    }

    private boolean isOwner(Wallet wallet) {
        return wallet.getOwner().equals(authUtils.getAuthenticatedUser());
    }

    private void enforceOneWalletPerCurrency(Currency currency, User owner) {
        if (walletRepository.existsByCurrencyAndOwner(currency, owner)) {
            throw new ImpossibleOperationException(String.format(WALLET_DUPLICATE, currency));
        }
    }

    private void stopWalletDeletionIfLastWallet(Wallet wallet) {
        if(walletRepository.findAllByOwner(wallet.getOwner()).size() == 1) {
            throw new ImpossibleOperationException(CANNOT_DELETE_LAST_WALLET);
        }
    }

    private void stopWalletDeletionIfBalancePositive(Wallet wallet) {
        if (wallet.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new ImpossibleOperationException(WALLET_STILL_HAS_FUNDS);
        }
    }
}
