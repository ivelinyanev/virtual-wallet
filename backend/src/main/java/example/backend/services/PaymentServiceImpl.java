package example.backend.services;

import example.backend.dtos.CardCreateReq;
import example.backend.dtos.CardMetaData;
import example.backend.enums.CardBrand;
import example.backend.exceptions.InvalidCardException;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static example.backend.utils.StringConstants.CARD_NUMBER_MUST_CONTAIN_ONLY_DIGITS;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Override
    public CardMetaData tokenize(CardCreateReq request) {

        String cardNumber = request.cardNumber();

        if (!cardNumber.matches("\\d+")) {
            throw new InvalidCardException(CARD_NUMBER_MUST_CONTAIN_ONLY_DIGITS);
        }

        CardBrand cardBrand = CardBrand.detect(cardNumber);

        if (!cardBrand.isValidLength(cardNumber)) {
            throw new InvalidCardException("Invalid card number for " + cardBrand);
        }

        return new CardMetaData(
                generateToken(),
                generateFingerprint(cardNumber, cardBrand),
                cardBrand,
                cardNumber.substring(cardNumber.length() - 4),
                request.expMonth(),
                request.expYear()
        );
    }

    /*
        Token is used to perform transactions
     */
    private String generateToken() {
        return "tok_" + UUID.randomUUID();
    }

    /*
        Fingerprint is used to identify card, applies per-user uniqueness (deduplication)
        since I am not storing sensitive information
     */
    private String generateFingerprint(String cardNumber, CardBrand cardBrand) {
        String raw = cardNumber + "|" + cardBrand;

        return DigestUtils.sha256Hex(raw);
    }
}
