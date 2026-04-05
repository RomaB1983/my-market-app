package ru.yandex.practicum.payment.config;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import ru.yandex.practicum.payment.model.User;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public RedisCacheManagerBuilderCustomizer userCacheCustomizer() {
        return builder -> builder.withCacheConfiguration(
                "user",                                         // Имя кеша
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.of(2, ChronoUnit.MINUTES))  // TTL
//                        .serializeValuesWith(                          // Сериализация JSON
//                                RedisSerializationContext.SerializationPair.fromSerializer(
//                                        new Jackson2JsonRedisSerializer<>(User.class)
//                                )
//                        )
        );
    }
}
