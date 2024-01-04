package com.pros.currencyconversionservice.service;

import com.pros.currencyconversionbase.exception.CurrencyConversionException;
import com.pros.currencyconversionservice.dto.CurrencyExchangeRateResponseDto;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * @author aldonavarreteluna
 * @version 1.0.0
 * @since 2023-12-31
 *
 * This interface defines the methods that the currency service must implement.
 */
public interface CurrencyService {

    /**
     * Converts a currency amount from one currency to another.
     *
     * @param source The source currency.
     * @param target The target currency.
     * @param date The date of the conversion (optional).
     * @return A mono containing the currency exchange rate response.
     */
    Mono<CurrencyExchangeRateResponseDto> convert(String source, String target, Instant date) throws CurrencyConversionException;
}
