package com.pros.currencyconversionservice.service.impl;

import com.pros.currencyconversionbase.exception.CurrencyConversionException;
import com.pros.currencyconversionbase.exception.CurrencyConversionMessageEnm;
import com.pros.currencyconversionbase.model.ExchangeRate;
import com.pros.currencyconversionbase.util.CacheUtil;
import com.pros.currencyconversionbase.util.ConstantUtil;
import com.pros.currencyconversionbase.util.CurrencyUtil;
import com.pros.currencyconversionservice.dto.CurrencyExchangeRateResponseDto;
import com.pros.currencyconversionservice.repository.ExchangeRateRepository;
import com.pros.currencyconversionservice.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * @author aldonavarreteluna
 * @version 1.0.0
 * @since 2023-12-31
 *
 * This class implements the currency service interface. It provides the implementation for the convert method, which converts a currency amount from one currency to another.
 *
 */

@Service
@RequiredArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyServiceImpl.class);

    private final ExchangeRateRepository exchangeRateRepository;
    private final ReactiveRedisTemplate<String, CurrencyExchangeRateResponseDto> reactiveRedisTemplate;


    @Override
    public Mono<CurrencyExchangeRateResponseDto> convert(String sourceCurrency, String targetCurrency, Instant date) throws CurrencyConversionException {

        logger.info("Currency conversion from currency {} to currency {} ", sourceCurrency, targetCurrency);

        Map<String, String> errorsValidation = validation(sourceCurrency, targetCurrency, date);

        if(!errorsValidation.isEmpty()){
            throw new CurrencyConversionException(CurrencyConversionMessageEnm.VALIDATION.name(), errorsValidation);
        }

        if(sourceCurrency.equals(targetCurrency)){
            return Mono.just(new CurrencyExchangeRateResponseDto(sourceCurrency, targetCurrency, BigDecimal.ONE, date));
        }


        if(date == null){
            date = Instant.now();
        }

        return findFirstExchangeRate(sourceCurrency, targetCurrency, date)
                .flatMap(rate -> Mono.just(new CurrencyExchangeRateResponseDto(sourceCurrency, targetCurrency, rate.getExchangeRate(), rate.getEffectiveStartDate() )))
                .doOnNext(cer -> logger.info("Currency exchange rate founded on MONGO DATABASE from currency {} to currency {} ", sourceCurrency, targetCurrency))
                .switchIfEmpty(inverseConversion(sourceCurrency, targetCurrency, date));
    }

    /**
     * This method validates the input parameters for the convert method.
     *
     * @param sourceCurrency The source currency.
     * @param targetCurrency The target currency.
     * @param date The date of the conversion (optional).
     * @return A map containing the validation errors.
     */
    private Map<String, String> validation(String sourceCurrency, String targetCurrency, Instant date){
        Map<String, String> errorsValidation = new HashMap<>();

        if(sourceCurrency == null || sourceCurrency.isEmpty()){
            errorsValidation.put("sourceCurrency", "This field is required");
        }else if(!CurrencyUtil.isValidCurrency(sourceCurrency)) {
            errorsValidation.put("sourceCurrency", "Source currency is not valid");
        }

        if(targetCurrency == null || targetCurrency.isEmpty()){
            errorsValidation.put("targetCurrency", "This field is required");
        }else if(!CurrencyUtil.isValidCurrency(targetCurrency)) {
            errorsValidation.put("targetCurrency", "Target currency is not valid");
        }

        if(date != null && date.isAfter(Instant.now())){
            errorsValidation.put("date", "The date cannot be later than the current date");
        }

        return errorsValidation;
    }

    /**
     * This method performs an inverse conversion, i.e., it converts the source currency to the target currency using the inverse of the exchange rate.
     *
     * @param sourceCurrency The source currency.
     * @param targetCurrency The target currency.
     * @param date The date of the conversion (optional).
     * @return A mono containing the currency exchange rate response.
     */
    protected Mono<CurrencyExchangeRateResponseDto> inverseConversion(String sourceCurrency, String targetCurrency, Instant date){

        return findFirstExchangeRate(targetCurrency, sourceCurrency,  date)
                .flatMap(rate -> Mono.just(
                        new CurrencyExchangeRateResponseDto(
                                sourceCurrency,
                                targetCurrency,
                                new BigDecimal(1).divide(rate.getExchangeRate(), ConstantUtil.PRECISION, RoundingMode.HALF_UP),
                                rate.getEffectiveStartDate())))
                .doOnNext(cer -> logger.info("Currency exchange rate calculated with INVERSE RATE from currency {} to currency {} ", targetCurrency, sourceCurrency))
                .switchIfEmpty(cacheTriangularConversion(sourceCurrency, targetCurrency, date));
    }

    /**
     * This method returns the cached response of the currency exchange rate for a triangular conversion.
     *
     * @param sourceCurrency The source currency.
     * @param targetCurrency The target currency.
     * @param date The date of the conversion (optional).
     * @return A mono containing the currency exchange rate response.
     */
    public Mono<CurrencyExchangeRateResponseDto> cacheTriangularConversion(String sourceCurrency, String targetCurrency, Instant date){

        return reactiveRedisTemplate.opsForValue().get(CacheUtil.defaultKey(sourceCurrency, targetCurrency, date))
                .doOnNext(cer -> logger.info("Currency exchange rate found in REDIS CACHE for key: {} and date: {}", CacheUtil.defaultKey(sourceCurrency, targetCurrency, date), date))
                .switchIfEmpty(triangularConversion(sourceCurrency, targetCurrency, date));
    }

    /**
     * This method calculates the currency exchange rate for a triangular conversion.
     *
     * @param sourceCurrency The source currency.
     * @param targetCurrency The target currency.
     * @param date The date of the conversion (optional).
     * @return A mono containing the currency exchange rate response.
     */
    public Mono<CurrencyExchangeRateResponseDto> triangularConversion(String sourceCurrency, String targetCurrency, Instant date) {

        return Flux.fromIterable(CurrencyUtil.baseCurrencies())
                .concatMap(currency -> {
                    Mono<ExchangeRate> firstExchangeRateMono = findFirstExchangeRate(sourceCurrency, currency.getCurrencyCode(), date);

                    Mono<ExchangeRate> secondExchangeRateMono = findFirstExchangeRate(currency.getCurrencyCode(), targetCurrency, date);

                    return Mono.zip(firstExchangeRateMono, secondExchangeRateMono)
                            .filter(tuple -> tuple.getT1() != null && tuple.getT2() != null)
                            .map(tuple -> Tuples.of(new CurrencyExchangeRateResponseDto(
                                    sourceCurrency, targetCurrency,
                                    tuple.getT1().getExchangeRate().multiply(tuple.getT2().getExchangeRate()),
                                    date
                            ), currency.getCurrencyCode()) );
                })
                .next()
                .doOnNext(tuple -> logger.info("Currency exchange rate calculated with TRIANGULAR CONVERSION with the middle currency: {} ", tuple.getT2()))
                .map(Tuple2::getT1)
                .flatMap(responseDto -> addCurrencyExchangeRate(CacheUtil.defaultKey(sourceCurrency, targetCurrency, date), responseDto).thenReturn(responseDto))
                .switchIfEmpty(baseConversion(sourceCurrency, targetCurrency, date));
    }

    /**
     * This method performs a base conversion, i.e., it converts the source currency to the target currency using the base currencies.
     *
     * @param sourceCurrency The source currency.
     * @param targetCurrency The target currency.
     * @param date The date of the conversion (optional).
     * @return A mono containing the currency exchange rate response.
     */
    private Mono<CurrencyExchangeRateResponseDto> baseConversion(String sourceCurrency, String targetCurrency, Instant date){

        return Flux.fromIterable(CurrencyUtil.baseCurrencies())
                .concatMap(currency -> {

                    Mono<ExchangeRate> firstExchangeRateMono = findFirstExchangeRate(currency.getCurrencyCode(), sourceCurrency, date);

                    Mono<ExchangeRate> secondExchangeRateMono = findFirstExchangeRate(currency.getCurrencyCode(), targetCurrency, date);

                    return Mono.zip(firstExchangeRateMono, secondExchangeRateMono)
                            .filter(tuple -> tuple.getT1() != null && tuple.getT2() != null)
                            .map(tuple -> Tuples.of(new CurrencyExchangeRateResponseDto(
                                    sourceCurrency, targetCurrency,
                                    tuple.getT2().getExchangeRate().divide(tuple.getT1().getExchangeRate(), ConstantUtil.PRECISION, RoundingMode.HALF_UP),
                                    date
                            ), currency.getCurrencyCode()));
                })
                .next()
                .doOnNext(tuple -> logger.info("Currency exchange rate calculated with BASE CONVERSION with the middle currency: {} ", tuple.getT2() ))
                .map(Tuple2::getT1)
                .flatMap(responseDto -> addCurrencyExchangeRate(CacheUtil.defaultKey(sourceCurrency, targetCurrency, date), responseDto).thenReturn(responseDto))
                .switchIfEmpty(Mono.empty());
    }

    /**
     * This method finds the first exchange rate for a given source currency, target currency, and date.
     *
     * @param sourceCurrency The source currency.
     * @param targetCurrency The target currency.
     * @param date The date of the conversion (optional).
     * @return A mono containing the exchange rate.
     */
    protected Mono<ExchangeRate> findFirstExchangeRate(String sourceCurrency, String targetCurrency, Instant date) {
        return exchangeRateRepository.findFirstBySourceCurrencyAndTargetCurrencyAndEffectiveStartDateLessThanEqual(
                sourceCurrency, targetCurrency, date, Sort.by(Sort.Direction.DESC, "effectiveStartDate")
        );
    }

    /**
     * This method adds the currency exchange rate response to the Redis cache.
     *
     * @param key The cache key.
     * @param dto The currency exchange rate response.
     * @return A mono containing the result of the operation.
     */
    public Mono<Boolean> addCurrencyExchangeRate(String key, CurrencyExchangeRateResponseDto dto) {
        return reactiveRedisTemplate.opsForValue().set(key, dto, Duration.ofHours(1));
    }
}
