package example.backend.services.implementations;

import example.backend.enums.Currency;
import example.backend.services.protocols.ConversionService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Primary
@Service
public class CachedConversionServiceImpl implements ConversionService {

    private final ConversionService delegate;

    public CachedConversionServiceImpl(@Qualifier("conversionServiceImpl") ConversionService delegate) {
        this.delegate = delegate;
    }

    @Override
    @Cacheable("exchangeRates")
    public BigDecimal getRate(Currency from, Currency to) {
        return delegate.getRate(from, to);
    }

    @Override
    public BigDecimal convert(Currency from, Currency to, BigDecimal amount) {
        return getRate(from, to).multiply(amount);
    }
}
