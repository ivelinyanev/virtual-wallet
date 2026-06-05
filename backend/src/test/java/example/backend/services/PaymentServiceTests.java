package example.backend.services;

import example.backend.dtos.card.CardCreateRequest;
import example.backend.dtos.card.CardTokenizationResult;
import example.backend.enums.CardBrand;
import example.backend.exceptions.InvalidCardException;
import example.backend.services.implementations.PaymentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static example.backend.utils.StringConstants.CARD_BRAND_UNSUPPORTED;
import static example.backend.utils.StringConstants.CARD_NUMBER_MUST_CONTAIN_ONLY_DIGITS;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTests {

    private final PaymentServiceImpl paymentService = new PaymentServiceImpl();

    @Test
    void tokenize_Should_ThrowException_When_CardNumberIsNotOnlyDigits() {
        CardCreateRequest request = new CardCreateRequest(
                "123412341234ABCD",
                "John",
                "Doe",
                10,
                2026,
                "123"
        );

        InvalidCardException ex = assertThrows(
                InvalidCardException.class,
                () -> paymentService.tokenize(request)
        );

        assertEquals(CARD_NUMBER_MUST_CONTAIN_ONLY_DIGITS, ex.getMessage());
    }

    @Test
    void tokenize_Should_ThrowException_When_CardNumberIsNotWithValidLength() {
        CardCreateRequest request = new CardCreateRequest(
                "41111111111111111", // VISA but invalid length
                "John",
                "Doe",
                10,
                2026,
                "123"
        );

        InvalidCardException ex = assertThrows(
                InvalidCardException.class,
                () -> paymentService.tokenize(request)
        );

        assert(ex.getMessage().startsWith("Invalid card number for"));
    }

    @Test
    void tokenize_Should_ThrowException_When_CardBrandIsNotValid() {
        CardCreateRequest request = new CardCreateRequest(
                "1234123412341234",
                "John",
                "Doe",
                10,
                2026,
                "123"
        );

        IllegalArgumentException ex = assertThrows(
                  IllegalArgumentException.class,
                  () -> paymentService.tokenize(request)
          );

        assertEquals(CARD_BRAND_UNSUPPORTED, ex.getMessage());
    }

    @Test
    void tokenize_Should_ReturnCardTokenizationResult_When_CardIsValid() {
        CardCreateRequest request = new CardCreateRequest(
                "4111111111111111", // VISA with valid length
                "John",
                "Doe",
                10,
                2026,
                "123"
        );

        CardTokenizationResult cardMetaData = paymentService.tokenize(request);

        assertNotNull(cardMetaData, "PaymentService.tokenize() should return a non-null CardTokenizationResult");
        assertEquals("1111", cardMetaData.last4(), "Last 4 digits should match");
        assertEquals(CardBrand.VISA, cardMetaData.cardBrand(), "Card brand should be VISA");
        assertNotNull(cardMetaData.fingerprint(), "Fingerprint should not be null");
        assertNotNull(cardMetaData.token(), "Token should not be null");
    }

}
