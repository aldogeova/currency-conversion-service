package com.pros.currencyconversionservice.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pros.currencyconversionservice.dto.CurrencyExchangeRateResponseDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author aldonavarreteluna
 * @version 1.0.0
 * @since 2024-01-02
 */

@Configuration
public class RedisReactiveConfiguration {


    @Bean
    public ReactiveRedisTemplate<String, CurrencyExchangeRateResponseDto> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
        Jackson2JsonRedisSerializer<CurrencyExchangeRateResponseDto> serializer = new CustomJackson2JsonRedisSerializer<>(CurrencyExchangeRateResponseDto.class);
        RedisSerializationContext.RedisSerializationContextBuilder<String, CurrencyExchangeRateResponseDto> builder =RedisSerializationContext.newSerializationContext(new StringRedisSerializer());
        RedisSerializationContext<String, CurrencyExchangeRateResponseDto> context = builder.value(serializer).build();
        return new ReactiveRedisTemplate<>(factory, context);
    }


}
