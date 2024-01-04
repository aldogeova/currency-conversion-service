package com.pros.currencyconversionservice.service.impl;

import com.pros.currencyconversionbase.model.ExchangeRate;
import com.pros.currencyconversionbase.util.CacheUtil;
import com.pros.currencyconversionservice.dto.CurrencyExchangeRateResponseDto;
import com.pros.currencyconversionservice.repository.ExchangeRateRepository;
import com.pros.currencyconversionservice.utils.TestLogAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import ch.qos.logback.classic.Logger;

import java.math.BigDecimal;
import java.time.Instant;
import static org.mockito.Mockito.when;


import static org.junit.jupiter.api.Assertions.*;

/**
 * @author aldonavarreteluna
 * @version 1.0.0
 * @since 2024-01-03
 */
@ExtendWith(MockitoExtension.class)
class CurrencyServiceImplTest {

    @InjectMocks
    private CurrencyServiceImpl currencyService;

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private ReactiveRedisTemplate<String, CurrencyExchangeRateResponseDto> reactiveRedisTemplate;

    @Mock
    private ReactiveValueOperations<String, CurrencyExchangeRateResponseDto> valueOps;

    private TestLogAppender testLogAppender;

    @BeforeEach
    void setUp() {
        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOps);
        Logger logger = (Logger) LoggerFactory.getLogger(CurrencyServiceImpl.class);
        testLogAppender = new TestLogAppender();
        testLogAppender.start();
        logger.addAppender(testLogAppender);
    }

    @AfterEach
    public void tearDown() {
        Logger logger = (Logger) LoggerFactory.getLogger(CurrencyServiceImpl.class);
        logger.detachAppender(testLogAppender);
    }

    String sourceCurrency = "EUR";
    String targetCurrency = "USD";

    Instant date = Instant.now();
    CurrencyExchangeRateResponseDto currencyExchangeRateResponseDto = new CurrencyExchangeRateResponseDto(
            sourceCurrency,
            targetCurrency,
            new BigDecimal("1.1234"),
            date);

    CurrencyExchangeRateResponseDto currencyExchangeRateResponseInverseDto = new CurrencyExchangeRateResponseDto(
            sourceCurrency,
            targetCurrency,
            new BigDecimal("1.1233999999"),
            date);


    ExchangeRate exchangeRate = new ExchangeRate(
            "6591902cca0d00331f90fbda",
            sourceCurrency,
            targetCurrency,
            new BigDecimal("1.1234"),
            date);

    ExchangeRate exchangeRateInverse = new ExchangeRate(
            "6591902cca0d00331f90fbdn",
            sourceCurrency,
            targetCurrency,
            new BigDecimal("0.8901548870"),
            date);



    @Test
    void foundOnMongoDB() throws Exception {
        // Given

        when(exchangeRateRepository.
                findFirstBySourceCurrencyAndTargetCurrencyAndEffectiveStartDateLessThanEqual(sourceCurrency, targetCurrency, date, Sort.by(Sort.Direction.DESC, "effectiveStartDate")))
                    .thenReturn(Mono.just(exchangeRate));

        when(exchangeRateRepository.
                findFirstBySourceCurrencyAndTargetCurrencyAndEffectiveStartDateLessThanEqual(targetCurrency, sourceCurrency, date, Sort.by(Sort.Direction.DESC, "effectiveStartDate")))
                .thenReturn(Mono.empty());

        when(valueOps.get(CacheUtil.defaultKey(sourceCurrency, targetCurrency, date))).thenReturn(Mono.empty());


        // When
        Mono<CurrencyExchangeRateResponseDto> responseEntityMono = currencyService.convert(sourceCurrency, targetCurrency, date);

        // Then
        CurrencyExchangeRateResponseDto responseEntity = responseEntityMono.block();

        assertTrue(testLogAppender.getLogs().stream()
                .anyMatch(event -> event.getFormattedMessage()
                        .contains("Currency exchange rate founded on MONGO DATABASE from currency EUR to currency USD")
                ));

        assertNotNull(responseEntity);
        assertEquals(currencyExchangeRateResponseDto, responseEntity);
    }


    @Test
    void foundOnMongoDBWithInverseQuery() throws Exception {
        // Given

        when(exchangeRateRepository.
                findFirstBySourceCurrencyAndTargetCurrencyAndEffectiveStartDateLessThanEqual(sourceCurrency, targetCurrency, date, Sort.by(Sort.Direction.DESC, "effectiveStartDate")))
                .thenReturn(Mono.empty());

        when(exchangeRateRepository.
                findFirstBySourceCurrencyAndTargetCurrencyAndEffectiveStartDateLessThanEqual(targetCurrency, sourceCurrency, date, Sort.by(Sort.Direction.DESC, "effectiveStartDate")))
                .thenReturn(Mono.just(exchangeRateInverse));

        when(valueOps.get(CacheUtil.defaultKey(sourceCurrency, targetCurrency, date))).thenReturn(Mono.empty());


        // When
        Mono<CurrencyExchangeRateResponseDto> responseEntityMono = currencyService.convert(sourceCurrency, targetCurrency, date);

        // Then
        CurrencyExchangeRateResponseDto responseEntity = responseEntityMono.block();

        assertTrue(testLogAppender.getLogs().stream()
                .anyMatch(event -> event.getFormattedMessage()
                        .contains("Currency exchange rate calculated with INVERSE RATE from currency USD to currency EUR")
                ));

        assertNotNull(responseEntity);
        assertEquals(currencyExchangeRateResponseInverseDto, responseEntity);
    }


    @Test
    void foundOnRedisDBCache() throws Exception {
        // Given

        when(exchangeRateRepository.
                findFirstBySourceCurrencyAndTargetCurrencyAndEffectiveStartDateLessThanEqual(sourceCurrency, targetCurrency, date, Sort.by(Sort.Direction.DESC, "effectiveStartDate")))
                .thenReturn(Mono.empty());

        when(exchangeRateRepository.
                findFirstBySourceCurrencyAndTargetCurrencyAndEffectiveStartDateLessThanEqual(targetCurrency, sourceCurrency, date, Sort.by(Sort.Direction.DESC, "effectiveStartDate")))
                .thenReturn(Mono.empty());

        when(valueOps.get(CacheUtil.defaultKey(sourceCurrency, targetCurrency, date))).thenReturn(Mono.just(currencyExchangeRateResponseDto));


        // When
        Mono<CurrencyExchangeRateResponseDto> responseEntityMono = currencyService.convert(sourceCurrency, targetCurrency, date);

        // Then
        CurrencyExchangeRateResponseDto responseEntity = responseEntityMono.block();

        assertTrue(testLogAppender.getLogs().stream()
                .anyMatch(event -> event.getFormattedMessage()
                        .contains("Currency exchange rate found in REDIS CACHE for key: EUR-USD-2024-01")
                ));

        assertNotNull(responseEntity);
        assertEquals(currencyExchangeRateResponseDto, responseEntity);
    }


}