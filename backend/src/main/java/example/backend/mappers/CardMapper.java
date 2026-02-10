package example.backend.mappers;

import example.backend.dtos.card.CardMetaData;
import example.backend.dtos.card.PrivateCardDto;
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
        card.setExpirationMonth(cardMetaData.expMonth());
        card.setExpirationYear(cardMetaData.expYear());

        return card;
    }


    public PrivateCardDto toPrivateCardDto(Card card) {

        return new PrivateCardDto(
                card.getId(),
                card.getCardBrand(),
                card.getLast4(),
                card.getExpirationMonth(),
                card.getExpirationYear(),
                card.getCardHolder().getFirstName() + " " + card.getCardHolder().getLastName()
        );
    }
}
