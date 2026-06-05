package example.backend.mappers;

import example.backend.dtos.card.CardTokenizationResult;
import example.backend.dtos.card.CardResponse;
import example.backend.models.Card;
import org.springframework.stereotype.Component;

public class CardMapper {

    public static Card toCard(CardTokenizationResult cardMetaData) {
        Card card = new Card();
        card.setToken(cardMetaData.token());
        card.setFingerprint(cardMetaData.fingerprint());
        card.setCardBrand(cardMetaData.cardBrand());
        card.setLast4(cardMetaData.last4());
        card.setExpirationMonth(cardMetaData.expMonth());
        card.setExpirationYear(cardMetaData.expYear());

        return card;
    }


    public static CardResponse toCardResponse(Card card) {

        return new CardResponse(
                card.getId(),
                card.getCardBrand(),
                card.getLast4(),
                card.getExpirationMonth(),
                card.getExpirationYear(),
                card.getCardHolder().getFirstName() + " " + card.getCardHolder().getLastName()
        );
    }
}
