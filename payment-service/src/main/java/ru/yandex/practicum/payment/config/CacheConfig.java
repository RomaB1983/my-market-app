package ru.yandex.practicum.payment.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public ReactiveRedisTemplate<String, Long> saldoRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory
    ) {
        RedisSerializationContext<String, Long> serializationContext =
                RedisSerializationContext.<String, Long>newSerializationContext(RedisSerializer.string())
                        .value(new Jackson2JsonRedisSerializer<>(Long.class))
                        .build();

        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }
}
