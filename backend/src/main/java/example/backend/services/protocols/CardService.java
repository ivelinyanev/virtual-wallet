package example.backend.services.protocols;

import example.backend.dtos.card.CardCreateRequest;
import example.backend.models.Card;

import java.util.List;

public interface CardService {

    List<Card> getMyCards();

    /*
        For support and audit uses only
     */
    List<Card> getCardsByUserId(Long userId);

    Card getById(Long id);

    Card create(CardCreateRequest request);

    void delete(Long id);
}
