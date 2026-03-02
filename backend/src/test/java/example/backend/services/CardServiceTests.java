package example.backend.services;

import example.backend.dtos.card.CardCreateReq;
import example.backend.dtos.card.CardMetaData;
import example.backend.enums.CardBrand;
import example.backend.exceptions.AuthorizationException;
import example.backend.exceptions.DuplicateException;
import example.backend.exceptions.EntityNotFoundException;
import example.backend.mappers.CardMapper;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CardServiceTests {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private AuthUtils authUtils;

    @InjectMocks
    private CardServiceImpl cardService;

    @Test
    void getMyCards_Should_ReturnMyCards() {
        User user = new User();
        List<Card> cards = List.of(new Card(), new Card());

        when(authUtils.getAuthenticatedUser()).thenReturn(user);
        when(cardRepository.findAllByCardHolder(user)).thenReturn(cards);

        List<Card> actualCards = cardService.getMyCards();

        assertEquals(2, actualCards.size());
        verify(authUtils, Mockito.times(1)).getAuthenticatedUser();
        verify(cardRepository, Mockito.times(1)).findAllByCardHolder(user);
        verify(cardRepository, Mockito.never()).findAll();
    }

    @Test
    void getCardsByUserId_Should_ReturnCardsByUserId() {
        Long userId = 10L;

        User user = new User();
        user.setId(userId);

        Card card1 = new Card();
        Card card2 = new Card();

        card1.setCardHolder(user);
        card2.setCardHolder(user);

        List<Card> cards = List.of(card1, card2);

        when(cardRepository.findAllByCardHolderId(userId)).thenReturn(cards);

        List<Card> actualCards = cardService.getCardsByUserId(userId);

        assertEquals(2, actualCards.size());
        assertTrue(actualCards.contains(card1));
        assertTrue(actualCards.contains(card2));
    }

    @Test
    void getById_Should_ReturnCardById() {
        Card card = new Card();
        card.setId(5L);

        when(cardRepository.findById(5L)).thenReturn(Optional.of(card));
        Card actualCard = cardService.getById(5L);

        assertEquals(card, actualCard);
        verify(cardRepository, Mockito.times(1)).findById(5L);
    }

    @Test
    void getById_Should_ThrowException_IfCardNotFound() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> cardService.getById(99L));

        verify(cardRepository, Mockito.times(1)).findById(99L);
    }

    @Test
    void create_Should_SaveCard_When_NewCardIsCreated() {
        User actingUser = new User();
        actingUser.setUsername("testUser");

        CardCreateReq request = new CardCreateReq(
                "1234123412341234",
                "John",
                "Doe",
                10,
                2026,
                "353"
        );

        CardMetaData cardMetaData = new CardMetaData(
                "tok_123",
                "fingerprint_123",
                CardBrand.VISA,
                "1234",
                10,
                2026
        );

        Card cardEntity = new Card();
        cardEntity.setCardHolder(actingUser);

        when(authUtils.getAuthenticatedUser()).thenReturn(actingUser);
        when(paymentService.tokenize(request)).thenReturn(cardMetaData);
        when(cardRepository.existsByFingerprintAndCardHolder(cardMetaData.fingerprint(), actingUser))
                .thenReturn(false);
        when(cardMapper.toCard(cardMetaData)).thenReturn(cardEntity);
        when(cardRepository.save(cardEntity)).thenReturn(cardEntity);

        Card actualCard = cardService.create(request);

        assertEquals(cardEntity, actualCard);
        assertEquals(actingUser, actualCard.getCardHolder());
        verify(cardRepository, Mockito.times(1)).save(cardEntity);
    }

    @Test
    void create_Should_ThrowException_IfCardAlreadyExists() {
        User actingUser = new User();
        actingUser.setUsername("testUser");

        CardCreateReq request = new CardCreateReq(
                "1234123412341234",
                "John",
                "Doe",
                10,
                2026,
                "353"
        );

        CardMetaData cardMetaData = new CardMetaData(
                "tok_123",
                "fingerprint_123",
                CardBrand.VISA,
                "1234",
                10,
                2026
        );

        when(authUtils.getAuthenticatedUser()).thenReturn(actingUser);
        when(paymentService.tokenize(request)).thenReturn(cardMetaData);
        when(cardRepository.existsByFingerprintAndCardHolder(cardMetaData.fingerprint(), actingUser))
                .thenReturn(true);

        assertThrows(DuplicateException.class, () -> cardService.create(request));
        verify(cardRepository, Mockito.times(0)).save(any());
    }

    @Test
    void delete_Should_DeleteCard_When_CardIsFound() {
        User actingUser = new User();
        actingUser.setUsername("testUser");

        Card card = new Card();
        card.setId(5L);
        card.setCardHolder(actingUser);

        when(authUtils.getAuthenticatedUser()).thenReturn(actingUser);
        when(cardRepository.findById(5L)).thenReturn(Optional.of(card));

        cardService.delete(5L);

        verify(cardRepository, Mockito.times(1)).findById(5L);
        verify(cardRepository, Mockito.times(1)).delete(card);
    }

    @Test
    void delete_Should_ThrowException_IfUserIsNotOwner() {
        User actingUser = new User();
        actingUser.setUsername("testUser");

        User owner = new User();
        actingUser.setUsername("owner");

        Card card = new Card();
        card.setId(5L);
        card.setCardHolder(owner);

        when(authUtils.getAuthenticatedUser()).thenReturn(actingUser);
        when(cardRepository.findById(5L)).thenReturn(Optional.of(card));

        assertThrows(AuthorizationException.class, () -> cardService.delete(5L));

        verify(cardRepository, Mockito.times(1)).findById(5L);
        verify(cardRepository, Mockito.times(0)).delete(any());
    }
}
