package example.backend.services;

import example.backend.dtos.card.CardCreateReq;
import example.backend.dtos.card.CardMetaData;
import example.backend.exceptions.AuthorizationException;
import example.backend.exceptions.DuplicateException;
import example.backend.exceptions.EntityNotFoundException;
import example.backend.mappers.CardMapper;
import example.backend.models.Card;
import example.backend.models.User;
import example.backend.repositories.CardRepository;
import example.backend.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static example.backend.utils.StringConstants.CARD_ALREADY_ADDED;
import static example.backend.utils.StringConstants.NOT_ALLOWED_TO_DELETE_CARD;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final PaymentService paymentService;
    private final CardMapper cardMapper;
    private final AuthUtils authUtils;

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<Card> getCards() {
        return cardRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('USER')")
    public List<Card> getMyCards() {
        User actingUser = authUtils.getAuthenticatedUser();

        return cardRepository.findAllByCardHolder(actingUser);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Card getById(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Card", "id", String.valueOf(id)));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('USER')")
    public Card create(CardCreateReq request) {
        User actingUser = authUtils.getAuthenticatedUser();

        CardMetaData metaData = paymentService.tokenize(request);

        if (cardRepository.existsByFingerprintAndCardHolder(metaData.fingerprint(), actingUser)) {
            throw new DuplicateException(CARD_ALREADY_ADDED);
        }

        Card card = cardMapper.toCard(metaData);
        card.setCardHolder(actingUser);

        return cardRepository.save(card);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('USER')")
    public void delete(Long id) {
        User actingUser = authUtils.getAuthenticatedUser();

        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Card", "id", String.valueOf(id)));

        if (!actingUser.equals(card.getCardHolder())) {
            throw new AuthorizationException(NOT_ALLOWED_TO_DELETE_CARD);
        }

        cardRepository.delete(card);
    }
}
