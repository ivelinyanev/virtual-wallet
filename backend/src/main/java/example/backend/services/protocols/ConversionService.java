package example.backend.services.protocols;

import example.backend.enums.Currency;

import java.math.BigDecimal;

public interface ConversionService {

    BigDecimal convert(Currency from, Currency to, BigDecimal amount);

}
