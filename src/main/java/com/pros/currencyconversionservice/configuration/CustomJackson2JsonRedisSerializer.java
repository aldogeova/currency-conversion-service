package com.pros.currencyconversionservice.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * @author aldonavarreteluna
 * @version 1.0.0
 * @since 2024-01-02
 */
public class CustomJackson2JsonRedisSerializer<T> extends Jackson2JsonRedisSerializer<T> {

    private final ObjectMapper objectMapper;
    private final Class<T> type;

    public CustomJackson2JsonRedisSerializer(Class<T> type) {
        super(type);
        this.type = type;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        // Puedes añadir más configuraciones a objectMapper si es necesario
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        try {
            return objectMapper.writeValueAsBytes(t);
        } catch (Exception e) {
            throw new SerializationException("Error al serializar el objeto a JSON", e);
        }
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return objectMapper.readValue(bytes, 0, bytes.length, type);
        } catch (Exception e) {
            throw new SerializationException("Error al deserializar el objeto de JSON", e);
        }
    }
}
