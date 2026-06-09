package example.backend.services;

import example.backend.dtos.card.CardCreateRequest;
import example.backend.dtos.card.CardTokenizationResult;
import example.backend.enums.CardBrand;
import example.backend.exceptions.AuthorizationException;
import example.backend.exceptions.CardExpiredException;
import example.backend.exceptions.DuplicateException;
import example.backend.exceptions.EntityNotFoundException;
import example.backend.models.Card;
import example.backend.models.User;
import example.backend.repositories.CardRepository;
import example.backend.services.implementations.CardServiceImpl;
import example.backend.services.protocols.PaymentService;
import example.backend.utils.AuthUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceTests {

    @Mock private CardRepository cardRepository;
    @Mock private PaymentService paymentService;
    @Mock private AuthUtils authUtils;

    @InjectMocks
    private CardServiceImpl cardService;

    // ───────────────────────── getMyCards ─────────────────────────

    @Test
    void getMyCards_Should_ReturnCardsForAuthenticatedUser() {
        User user = new User();
        List<Card> cards = List.of(new Card(), new Card());

        when(authUtils.getAuthenticatedUser()).thenReturn(user);
        when(cardRepository.findAllByCardHolderAndDeletedFalse(user)).thenReturn(cards);

        List<Card> result = cardService.getMyCards();

        assertEquals(2, result.size());
        verify(cardRepository, times(1)).findAllByCardHolderAndDeletedFalse(user);
        verify(cardRepository, never()).findAll();
    }

    // ───────────────────────── getCardsByUserId ─────────────────────────

    @Test
    void getCardsByUserId_Should_ReturnCardsForGivenUser() {
        Long userId = 10L;
        User user = new User();
        user.setId(userId);

        Card card1 = new Card();
        Card card2 = new Card();
        card1.setCardHolder(user);
        card2.setCardHolder(user);

        when(cardRepository.findAllByCardHolderIdAndDeletedFalse(userId)).thenReturn(List.of(card1, card2));

        List<Card> result = cardService.getCardsByUserId(userId);

        assertEquals(2, result.size());
        assertTrue(result.contains(card1));
        assertTrue(result.contains(card2));
    }

    // ───────────────────────── getById ─────────────────────────

    @Test
    void getById_Should_ReturnCard_When_Found() {
        Card card = new Card();
        card.setId(5L);

        when(cardRepository.findById(5L)).thenReturn(Optional.of(card));

        Card result = cardService.getById(5L);

        assertEquals(card, result);
        verify(cardRepository, times(1)).findById(5L);
    }

    @Test
    void getById_Should_Throw_When_NotFound() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> cardService.getById(99L));
    }

    // ───────────────────────── create ─────────────────────────

    @Test
    void create_Should_SaveCard_When_Valid() {
        User actingUser = new User();
        actingUser.setUsername("testUser");

        int futureYear = LocalDate.now().getYear() + 1;

        CardCreateRequest request = new CardCreateRequest("4111111111111111", "John", "Doe", 1, futureYear, "123");
        CardTokenizationResult metaData = new CardTokenizationResult("tok_123", "fp_123", CardBrand.VISA, "1111", 1, futureYear);

        when(authUtils.getAuthenticatedUser()).thenReturn(actingUser);
        when(paymentService.tokenize(request)).thenReturn(metaData);
        when(cardRepository.existsByFingerprintAndCardHolder("fp_123", actingUser)).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        Card result = cardService.create(request);

        assertNotNull(result);
        assertEquals(actingUser, result.getCardHolder());
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void create_Should_Throw_When_CardAlreadyAdded() {
        User actingUser = new User();
        actingUser.setUsername("testUser");

        int futureYear = LocalDate.now().getYear() + 1;

        CardCreateRequest request = new CardCreateRequest("4111111111111111", "John", "Doe", 1, futureYear, "123");
        CardTokenizationResult metaData = new CardTokenizationResult("tok_123", "fp_123", CardBrand.VISA, "1111", 1, futureYear);

        when(authUtils.getAuthenticatedUser()).thenReturn(actingUser);
        when(paymentService.tokenize(request)).thenReturn(metaData);
        when(cardRepository.existsByFingerprintAndCardHolder("fp_123", actingUser)).thenReturn(true);

        assertThrows(DuplicateException.class, () -> cardService.create(request));
        verify(cardRepository, never()).save(any());
    }

    @Test
    void create_Should_Throw_When_CardIsExpired_PastYear() {
        int pastYear = LocalDate.now().getYear() - 1;
        CardCreateRequest request = new CardCreateRequest("4111111111111111", "John", "Doe", 12, pastYear, "123");

        assertThrows(CardExpiredException.class, () -> cardService.create(request));
        verify(paymentService, never()).tokenize(any());
    }

    @Test
    void create_Should_Throw_When_CardIsExpired_PastMonthSameYear() {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int pastMonth = now.getMonthValue() - 1;

        // only valid if we are not in January (month 1)
        if (pastMonth < 1) return;

        CardCreateRequest request = new CardCreateRequest("4111111111111111", "John", "Doe", pastMonth, currentYear, "123");

        assertThrows(CardExpiredException.class, () -> cardService.create(request));
        verify(paymentService, never()).tokenize(any());
    }

    // ───────────────────────── delete ─────────────────────────

    @Test
    void delete_Should_DeleteCard_When_UserIsOwner() {
        User actingUser = new User();
        actingUser.setUsername("testUser");

        Card card = new Card();
        card.setId(5L);
        card.setCardHolder(actingUser);

        when(authUtils.getAuthenticatedUser()).thenReturn(actingUser);
        when(cardRepository.findByIdAndDeletedFalse(5L)).thenReturn(Optional.of(card));

        cardService.delete(5L);

        assertTrue(card.isDeleted());
        verify(cardRepository, times(1)).save(card);
        verify(cardRepository, never()).delete(any());
    }

    @Test
    void delete_Should_Throw_When_UserIsNotOwner() {
        User actingUser = new User();
        actingUser.setUsername("actingUser");

        User owner = new User();
        owner.setUsername("owner");

        Card card = new Card();
        card.setId(5L);
        card.setCardHolder(owner);

        when(authUtils.getAuthenticatedUser()).thenReturn(actingUser);
        when(cardRepository.findByIdAndDeletedFalse(5L)).thenReturn(Optional.of(card));

        assertThrows(AuthorizationException.class, () -> cardService.delete(5L));
        verify(cardRepository, never()).delete(any());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void delete_Should_Throw_When_CardNotFound() {
        when(authUtils.getAuthenticatedUser()).thenReturn(new User());
        when(cardRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> cardService.delete(99L));
    }
}
