package example.backend.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record ExchangeRateApiResponse(

        @JsonProperty("conversion_result")
        BigDecimal conversionResult

) {
}
