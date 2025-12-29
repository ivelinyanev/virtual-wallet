package example.backend.mappers;

import example.backend.dtos.card.CardMetaData;
import example.backend.models.Card;
import org.springframework.stereotype.Component;

@Component
public class CardMapper {

    public Card toCard(CardMetaData cardMetaData) {
        Card card = new Card();
        card.setToken(cardMetaData.token());
        card.setFingerprint(cardMetaData.fingerprint());
        card.setCardBrand(cardMetaData.cardBrand());
        card.setLast4(cardMetaData.last4());
        card.setExpirationMonth(card.getExpirationMonth());
        card.setExpirationYear(card.getExpirationYear());

        return card;
    }

}
