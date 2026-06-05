package example.backend.dtos.card;

import example.backend.enums.CardBrand;

public record CardResponse(
        Long id,
        CardBrand cardBrand,
        String last4,
        int expirationMonth,
        int expirationYear,
        String cardHolder
) {
}
