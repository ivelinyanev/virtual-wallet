package example.backend.dtos;

import example.backend.enums.CardBrand;

public record CardMetaData(
        String token,
        String fingerprint,
        CardBrand cardBrand,
        String last4,
        Integer expMonth,
        Integer expYear
) {
}
