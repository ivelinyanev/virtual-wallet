package example.backend.services;

import example.backend.enums.Currency;
import example.backend.exceptions.*;
import example.backend.models.User;
import example.backend.models.Wallet;
import example.backend.repositories.WalletRepository;
import example.backend.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static example.backend.utils.StringConstants.*;

@RequiredArgsConstructor
@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final AuthUtils authUtils;

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('USER')")
    public List<Wallet> getMyWallets() {
        User actingUser = authUtils.getAuthenticatedUser();

        checkAccountLimitations(actingUser);

        return walletRepository.findAllByOwner(actingUser);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('USER')")
    public Wallet getWalletById(Long id) {
        checkAccountLimitations(authUtils.getAuthenticatedUser());

        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Wallet", "id", String.valueOf(id)));

        if (!isOwner(wallet)) {
            throw new AuthorizationException(CANNOT_ACCESS_WALLET_YOU_ARE_NOT_OWNER_OF);
        }

        return wallet;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('USER')")
    public Wallet createWallet(Wallet wallet) {
        User actingUser = authUtils.getAuthenticatedUser();
        checkAccountLimitations(actingUser);

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

    /*
        TODO: maybe add check for user to not be able to delete a wallet if the balance is positive
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('USER')")
    public void deleteWallet(Long id) {
        User actingUser = authUtils.getAuthenticatedUser();
        checkAccountLimitations(actingUser);

        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Wallet", "id", String.valueOf(id)));

        if (!isOwner(wallet)) {
            throw new AuthorizationException(CANNOT_DELETE_WALLET_YOU_ARE_NOT_OWNER_OF);
        }

        walletRepository.delete(wallet);
    }

    /*
        Helpers
     */

    private boolean isOwner(Wallet wallet) {
        return wallet.getOwner().equals(authUtils.getAuthenticatedUser());
    }

    private void checkAccountLimitations(User actingUser) {
        if (actingUser.isBlocked()) {
            throw new UserBlockedException(USER_BLOCKED);
        }

        if (!actingUser.isVerified()) {
            throw new AccountNotVerifiedException(ACCOUNT_NOT_VERIFIED);
        }
    }

    private void enforceOneWalletPerCurrency(Currency currency, User owner) {
        if (walletRepository.existsByCurrencyAndOwner(currency, owner)) {
            throw new ImpossibleOperationException(String.format(WALLET_DUPLICATE, currency));
        }
    }
}
