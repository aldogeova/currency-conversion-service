package com.pros.currencyconversionservice.controller;

import com.pros.currencyconversionservice.dto.CurrencyExchangeRateResponseDto;
import com.pros.currencyconversionservice.service.CurrencyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author aldonavarreteluna
 * @version 1.0.0
 * @since 2024-01-03
 * Currency Conversion Controller Test
 */


@ExtendWith(MockitoExtension.class)
class CurrencyConversionControllerTest {

    @InjectMocks
    private CurrencyConversionController currencyConversionController;

    @Mock
    private CurrencyService currencyService;

    /**
     * Test convertCurrency method
     * @throws Exception
     */
    @Test
    void convertCurrency_shouldReturnCurrencyExchangeRateResponseDto() throws Exception {
        // Given
        String sourceCurrency = "EUR";
        String targetCurrency = "USD";
        Instant date = Instant.now();
        CurrencyExchangeRateResponseDto currencyExchangeRateResponseDto = new CurrencyExchangeRateResponseDto(
                sourceCurrency,
                targetCurrency,
                new BigDecimal("1.1234"),
                date);
        when(currencyService.convert(sourceCurrency, targetCurrency, date)).thenReturn(Mono.just(currencyExchangeRateResponseDto));

        // When
        Mono<ResponseEntity<CurrencyExchangeRateResponseDto>> responseEntityMono = currencyConversionController.convertCurrency(sourceCurrency, targetCurrency, date);

        // Then
        ResponseEntity<CurrencyExchangeRateResponseDto> responseEntity = responseEntityMono.block();
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());
        assertEquals(currencyExchangeRateResponseDto, responseEntity.getBody());
    }

    /**
     * Test convertCurrency method
     * @throws Exception
     */
    @Test
    void convertCurrency_shouldReturnNotFound() throws Exception {
        // Given
        String sourceCurrency = "EUR";
        String targetCurrency = "USD";
        Instant date = Instant.now();
        when(currencyService.convert(sourceCurrency, targetCurrency, date)).thenReturn(Mono.empty());

        // When
        Mono<ResponseEntity<CurrencyExchangeRateResponseDto>> responseEntityMono = currencyConversionController.convertCurrency(sourceCurrency, targetCurrency, date);

        // Then
        ResponseEntity<CurrencyExchangeRateResponseDto> responseEntity = responseEntityMono.block();
        assertNotNull(responseEntity);
        assertEquals(404, responseEntity.getStatusCode().value());
    }
}