package example.backend.services.protocols;

import example.backend.models.Card;
import example.backend.models.User;
import example.backend.models.Wallet;

public interface AuthorizationService {

    User currentUser();

    void assertOwnsWallet(Wallet wallet);

    void assertOwnsCard(Card card);

    void assertVerifiedAndActive();
}
