package com.pros.currencyconversionservice.repository;

import com.pros.currencyconversionbase.model.ExchangeRate;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * @author aldonavarreteluna
 * @version 1.0.0
 * @since 2023-12-30
 *
 * This interface defines the methods that the exchange rate repository must implement.
 */
public interface ExchangeRateRepository extends ReactiveMongoRepository<ExchangeRate, String> {

    Mono<ExchangeRate> findFirstBySourceCurrencyAndTargetCurrencyAndEffectiveStartDateLessThanEqual(
            String sourceCurrency,
            String targetCurrency,
            Instant date,
            Sort sort);

}
