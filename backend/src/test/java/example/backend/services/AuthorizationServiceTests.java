package example.backend.services;

import example.backend.exceptions.AccountNotVerifiedException;
import example.backend.exceptions.AuthorizationException;
import example.backend.exceptions.UserBlockedException;
import example.backend.models.Card;
import example.backend.models.User;
import example.backend.models.Wallet;
import example.backend.security.CustomUserDetails;
import example.backend.services.implementations.AuthorizationServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AuthorizationServiceTests {

    private AuthorizationServiceImpl authorizationService;
    private User user;

    @BeforeEach
    void setUp() {
        authorizationService = new AuthorizationServiceImpl();

        user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setVerified(true);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ───────────────────────── currentUser ─────────────────────────

    @Test
    void currentUser_Should_ReturnActingUser() {
        assertEquals(user, authorizationService.currentUser());
    }

    // ───────────────────────── assertOwnsWallet ─────────────────────────

    @Test
    void assertOwnsWallet_Should_PassSilently_When_UserOwnsWallet() {
        Wallet wallet = new Wallet();
        wallet.setOwner(user);

        assertDoesNotThrow(() -> authorizationService.assertOwnsWallet(wallet));
    }

    @Test
    void assertOwnsWallet_Should_Throw_When_UserDoesNotOwnWallet() {
        User other = new User();
        other.setUsername("other");

        Wallet wallet = new Wallet();
        wallet.setOwner(other);

        assertThrows(AuthorizationException.class,
                () -> authorizationService.assertOwnsWallet(wallet));
    }

    // ───────────────────────── assertOwnsCard ─────────────────────────

    @Test
    void assertOwnsCard_Should_PassSilently_When_UserOwnsCard() {
        Card card = new Card();
        card.setId(1L);
        card.setCardHolder(user);

        assertDoesNotThrow(() -> authorizationService.assertOwnsCard(card));
    }

    @Test
    void assertOwnsCard_Should_Throw_When_UserDoesNotOwnCard() {
        User other = new User();
        other.setUsername("other");

        Card card = new Card();
        card.setId(1L);
        card.setCardHolder(other);

        assertThrows(AuthorizationException.class,
                () -> authorizationService.assertOwnsCard(card));
    }

    // ───────────────────────── assertVerifiedAndActive ─────────────────────────

    @Test
    void assertVerifiedAndActive_Should_PassSilently_When_VerifiedAndNotBlocked() {
        assertDoesNotThrow(() -> authorizationService.assertVerifiedAndActive());
    }

    @Test
    void assertVerifiedAndActive_Should_Throw_UserBlockedException_When_Blocked() {
        user.setBlocked(true);

        assertThrows(UserBlockedException.class,
                () -> authorizationService.assertVerifiedAndActive());
    }

    @Test
    void assertVerifiedAndActive_Should_Throw_AccountNotVerifiedException_When_NotVerified() {
        user.setVerified(false);

        assertThrows(AccountNotVerifiedException.class,
                () -> authorizationService.assertVerifiedAndActive());
    }
}
