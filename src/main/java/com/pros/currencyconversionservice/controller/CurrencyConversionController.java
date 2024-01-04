package com.pros.currencyconversionservice.controller;

import com.pros.currencyconversionbase.exception.CurrencyConversionException;
import com.pros.currencyconversionservice.dto.CurrencyExchangeRateResponseDto;
import com.pros.currencyconversionservice.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * @author aldonavarreteluna
 * @version 1.0.0
 * @since 2023-12-31
 * Currency Conversion Controller
 */

@RestController
@RequestMapping("/api/currency-conversion")
@RequiredArgsConstructor
public class CurrencyConversionController {

    private final CurrencyService currencyService;

    @GetMapping("/convert")
    @ApiResponse(responseCode = "200", description = "Currency conversion found or calculated",
                    content = { @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CurrencyExchangeRateResponseDto.class)) })
    @ApiResponse(responseCode = "404", description = "Currency conversion not found or it could not be calculated", content = {})
    @Operation(summary = "Get the exchange rate between two currencies with a optional date")
    public Mono<ResponseEntity<CurrencyExchangeRateResponseDto>> convertCurrency(
            @Parameter(description = "The source currency", example = "EUR")
            @RequestParam(required = false) String sourceCurrency,
            @Parameter(description = "The target currency", example = "USD")
            @RequestParam(required = false) String targetCurrency,
            @Parameter(description = "The OPTIONAL conversion date in the UTC timezone.", example = "2023-12-31T00:00:00Z")
            @RequestParam(required = false) Instant date
            ) throws CurrencyConversionException {
        return currencyService.convert(sourceCurrency, targetCurrency, date)
                .map(conversion -> ResponseEntity.ok().body(conversion))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}
