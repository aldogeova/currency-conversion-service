package com.pros.currencyconversionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * @author aldonavarreteluna
 * @version 1.0.0
 * @since 2023-12-31
 *
 * Currency Exchange Rate Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyExchangeRateResponseDto {

    private String sourceCurrency;

    private String targetCurrency;

    private BigDecimal exchangeRate;

    private Instant effectiveStartDate;

}
