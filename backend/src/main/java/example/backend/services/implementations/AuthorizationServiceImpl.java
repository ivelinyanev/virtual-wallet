package example.backend.services.implementations;

import example.backend.exceptions.AccountNotVerifiedException;
import example.backend.exceptions.AuthorizationException;
import example.backend.exceptions.UserBlockedException;
import example.backend.models.Card;
import example.backend.models.User;
import example.backend.models.Wallet;
import example.backend.security.CustomUserDetails;
import example.backend.services.protocols.AuthorizationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import static example.backend.utils.StringConstants.*;

@Service
public class AuthorizationServiceImpl implements AuthorizationService {

    @Override
    public User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ((CustomUserDetails) auth.getPrincipal()).getUser();
    }

    @Override
    public void assertOwnsWallet(Wallet wallet) {
        if (!wallet.getOwner().equals(currentUser())) {
            throw new AuthorizationException(YOU_ARE_NOT_THE_WALLET_OWNER);
        }
    }

    @Override
    public void assertOwnsCard(Card card) {
        if (!card.getCardHolder().equals(currentUser())) {
            throw new AuthorizationException(NOT_ALLOWED_TO_DELETE_CARD);
        }
    }

    @Override
    public void assertVerifiedAndActive() {
        User user = currentUser();
        if (user.isBlocked()) {
            throw new UserBlockedException(USER_BLOCKED);
        }
        if (!user.isVerified()) {
            throw new AccountNotVerifiedException(ACCOUNT_NOT_VERIFIED);
        }
    }
}
