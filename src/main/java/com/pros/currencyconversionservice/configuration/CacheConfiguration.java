package com.pros.currencyconversionservice.configuration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Configuration;

/**
 * @author aldonavarreteluna
 * @version 1.0.0
 * @since 2023-12-31
 */

@Configuration
@EnableCaching
public class CacheConfiguration {

    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("currencyExhangeRateResponseDtos");
    }

}
