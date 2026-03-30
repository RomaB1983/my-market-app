package ru.yandex.practicum.payment.config;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import ru.yandex.practicum.payment.model.User;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Configuration
public class CacheConfig {
    @Bean
    public RedisCacheManagerBuilderCustomizer weatherCacheCustomizer() {
        return builder -> builder.withCacheConfiguration(
                "weather",                                         // Имя кеша
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.of(2, ChronoUnit.MINUTES))  // TTL
                        .serializeValuesWith(                          // Сериализация JSON
                                RedisSerializationContext.SerializationPair.fromSerializer(
                                        new Jackson2JsonRedisSerializer<>(User.class)
                                )
                        )
        );
    }
}
