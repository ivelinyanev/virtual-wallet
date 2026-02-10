package example.backend.dtos.card;

import example.backend.enums.CardBrand;

public record PrivateCardDto(
        Long id,
        CardBrand cardBrand,
        String last4,
        int expirationMonth,
        int expirationYear,
        String cardHolder
) {
}
