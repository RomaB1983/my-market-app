package ru.yandex.practicum.shop.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import ru.yandex.practicum.shop.model.Item;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public ReactiveRedisTemplate<String, Item> itemRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory
    ) {
        RedisSerializationContext<String, Item> serializationContext =
                RedisSerializationContext.<String, Item>newSerializationContext(RedisSerializer.string())
                        .value(new Jackson2JsonRedisSerializer<>(Item.class))
                        .build();

        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }
}

