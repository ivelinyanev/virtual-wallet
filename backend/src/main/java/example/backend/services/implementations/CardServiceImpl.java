package example.backend.services.implementations;

import example.backend.annotations.RequiresVerifiedAccount;
import example.backend.dtos.card.CardCreateRequest;
import example.backend.dtos.card.CardTokenizationResult;
import example.backend.exceptions.AuthorizationException;
import example.backend.exceptions.CardExpiredException;
import example.backend.exceptions.DuplicateException;
import example.backend.exceptions.EntityNotFoundException;
import example.backend.mappers.CardMapper;
import example.backend.models.Card;
import example.backend.models.User;
import example.backend.repositories.CardRepository;
import example.backend.services.protocols.CardService;
import example.backend.services.protocols.PaymentService;
import example.backend.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.util.Date;
import java.util.List;

import static example.backend.utils.StringConstants.CARD_ALREADY_ADDED;
import static example.backend.utils.StringConstants.NOT_ALLOWED_TO_DELETE_CARD;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final PaymentService paymentService;
    private final AuthUtils authUtils;

    @Override
    @Transactional(readOnly = true)
    @RequiresVerifiedAccount
    @PreAuthorize("hasRole('USER')")
    public List<Card> getMyCards() {
        User actingUser = authUtils.getAuthenticatedUser();

        return cardRepository.findAllByCardHolderAndDeletedFalse(actingUser);
    }

    @Override
    @Transactional(readOnly = true)
    @RequiresVerifiedAccount
    @PreAuthorize("hasRole('ADMIN')")
    public List<Card> getCardsByUserId(Long userId) {
        return cardRepository.findAllByCardHolderIdAndDeletedFalse(userId);
    }

    @Override
    @Transactional(readOnly = true)
    @RequiresVerifiedAccount
    @PreAuthorize("hasRole('ADMIN')")
    public Card getById(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card", "id", String.valueOf(cardId)));
    }

    @Override
    @Transactional
    @RequiresVerifiedAccount
    @PreAuthorize("hasRole('USER')")
    public Card create(CardCreateRequest request) {
        checkCardExpired(request.expMonth(), request.expYear());

        User actingUser = authUtils.getAuthenticatedUser();
        CardTokenizationResult metaData = paymentService.tokenize(request);

        if (cardRepository.existsByFingerprintAndCardHolder(metaData.fingerprint(), actingUser)) {
            throw new DuplicateException(CARD_ALREADY_ADDED);
        }

        Card card = CardMapper.toCard(metaData);
        card.setCardHolder(actingUser);

        return cardRepository.save(card);
    }

    @Override
    @Transactional
    @RequiresVerifiedAccount
    @PreAuthorize("hasRole('USER')")
    public void delete(Long cardId) {
        User actingUser = authUtils.getAuthenticatedUser();

        Card card = cardRepository.findByIdAndDeletedFalse(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card", "id", String.valueOf(cardId)));

        if (!actingUser.equals(card.getCardHolder())) {
            throw new AuthorizationException(NOT_ALLOWED_TO_DELETE_CARD);
        }

        card.setDeleted(true);
        cardRepository.save(card);
    }

    private void checkCardExpired(int expirationMonth, int expirationYear) {
        int currentMonth = LocalDate.now().getMonth().getValue();
        int currentYear = LocalDate.now().getYear();

        if (expirationYear < currentYear) {
            throw new CardExpiredException("Provided card is expired");
        }

        if (expirationYear == currentYear && expirationMonth < currentMonth) {
            throw new CardExpiredException("Provided card is expired");
        }
    }
}
