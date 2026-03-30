package ru.yandex.practicum.shop.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.model.CartItem;

public interface CartItemRepository extends ReactiveCrudRepository<CartItem, Long> {
    Flux<CartItem> findBySessionId(String sessionId);

    Mono<CartItem> findBySessionIdAndItemId(String sessionId, Long itemId);

    Mono<Void> deleteBySessionId(String sessionId);
}
