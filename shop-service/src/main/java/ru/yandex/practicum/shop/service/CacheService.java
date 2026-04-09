package ru.yandex.practicum.shop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.model.Item;
import ru.yandex.practicum.shop.repository.ItemRepository;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {
    private static final String CACHE_PREFIX = "item:";
    private static final Duration TTL = Duration.ofMinutes(2);

    private final ItemRepository itemRepository;
    private final ReactiveRedisTemplate<String, Item> redisTemplate;

    @Transactional(readOnly = true)
    public Mono<Item> getItemById(String sessionId, Long id) {
        String keyCache = CACHE_PREFIX + id;
        return redisTemplate.opsForValue().get(keyCache)
                .doOnSuccess(saldo -> log.info("Продукт есть в кеше id:{}, sessionId:{}", id, sessionId))
                .switchIfEmpty(Mono.defer(() -> itemRepository.findById(id)
                        .flatMap(item -> redisTemplate.opsForValue()
                                .set(keyCache, item, TTL)
                                .doOnSuccess(ok ->
                                        log.info("Продукт положили в кеш id:{}, sessionId:{}", id, sessionId))
                                .thenReturn(item))));
    }
}
