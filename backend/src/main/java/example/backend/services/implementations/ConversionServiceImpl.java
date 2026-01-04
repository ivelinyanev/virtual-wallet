package example.backend.services.implementations;

import example.backend.dtos.ExchangeRateApiResponseDto;
import example.backend.enums.Currency;
import example.backend.exceptions.ExchangeRateApiException;
import example.backend.services.protocols.ConversionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Service
public class ConversionServiceImpl implements ConversionService {

    @Value("${exchange.rate.api.key}")
    private String key;

    private final RestClient client;

    public ConversionServiceImpl() {
        this.client = RestClient.create();
    }

    @Override
    public BigDecimal convert(Currency from, Currency to, BigDecimal amount) {
        try {
            String uri = "https://v6.exchangerate-api.com/v6/${key}/pair/${from}/${to}/${amount}";

            ExchangeRateApiResponseDto response;

            response = client
                    .get()
                    .uri(uri, key, from.toString(), to.toString(), amount)
                    .retrieve()
                    .body(ExchangeRateApiResponseDto.class);

            assert response != null;
            return response.conversionResult();
        } catch (Exception e) {
            throw new ExchangeRateApiException(e.getMessage());
        }
    }
}
