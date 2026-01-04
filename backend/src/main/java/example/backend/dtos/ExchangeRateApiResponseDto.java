package example.backend.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record ExchangeRateApiResponseDto(

        @JsonProperty("conversion_result")
        BigDecimal conversionResult

) {
}
