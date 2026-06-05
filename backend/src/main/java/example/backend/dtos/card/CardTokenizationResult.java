package example.backend.dtos.card;

import example.backend.enums.CardBrand;

public record CardTokenizationResult(
        String token,
        String fingerprint,
        CardBrand cardBrand,
        String last4,
        Integer expMonth,
        Integer expYear
) {
}
