package example.backend.services.protocols;

import example.backend.dtos.card.CardCreateReq;
import example.backend.models.Card;

import java.util.List;

public interface CardService {

    List<Card> getCards();

    List<Card> getMyCards();

    Card getById(Long id);

    Card create(CardCreateReq request);

    void delete(Long id);
}
