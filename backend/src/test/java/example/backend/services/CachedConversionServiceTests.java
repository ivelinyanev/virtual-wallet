package example.backend.services;

import com.github.benmanes.caffeine.cache.Caffeine;
import example.backend.enums.Currency;
import example.backend.exceptions.ExchangeRateApiException;
import example.backend.services.implementations.CachedConversionServiceImpl;
import example.backend.services.protocols.ConversionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CachedConversionServiceTests.Config.class)
public class CachedConversionServiceTests {

    @Configuration
    @EnableCaching
    static class Config {

        @Bean("conversionServiceImpl")
        ConversionService delegate() {
            return mock(ConversionService.class);
        }

        @Bean
        CachedConversionServiceImpl cachedConversionService(
                @Qualifier("conversionServiceImpl") ConversionService delegate) {
            return new CachedConversionServiceImpl(delegate);
        }

        @Bean
        CacheManager cacheManager() {
            CaffeineCacheManager manager = new CaffeineCacheManager("exchangeRates");
            manager.setCaffeine(Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES));
            return manager;
        }
    }

    @Autowired
    @Qualifier("cachedConversionService")
    private ConversionService service;

    @Autowired
    @Qualifier("conversionServiceImpl")
    private ConversionService delegate;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        reset(delegate);
        cacheManager.getCache("exchangeRates").clear();
    }

    @Test
    void getRate_Should_InvokeDelegateOnlyOnce_WhenCalledTwice() {
        when(delegate.getRate(Currency.EUR, Currency.USD)).thenReturn(new BigDecimal("1.1"));

        service.getRate(Currency.EUR, Currency.USD);
        service.getRate(Currency.EUR, Currency.USD);

        verify(delegate, times(1)).getRate(Currency.EUR, Currency.USD);
    }

    @Test
    void convert_Should_MultiplyGetRate_And_NotDelegateConvert() {
        when(delegate.getRate(Currency.EUR, Currency.USD)).thenReturn(new BigDecimal("1.1"));

        BigDecimal result = service.convert(Currency.EUR, Currency.USD, new BigDecimal("100"));

        assertEquals(0, result.compareTo(new BigDecimal("110.0")));
        verify(delegate, never()).convert(any(), any(), any());
    }

    @Test
    void getRate_Should_PropagateException_When_ApiThrows() {
        when(delegate.getRate(Currency.EUR, Currency.USD))
                .thenThrow(new ExchangeRateApiException("API error"));

        assertThrows(ExchangeRateApiException.class,
                () -> service.getRate(Currency.EUR, Currency.USD));
    }
}
