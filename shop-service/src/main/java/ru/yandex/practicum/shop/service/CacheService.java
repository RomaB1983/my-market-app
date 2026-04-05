package ru.yandex.practicum.shop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.dto.ItemDto;
import ru.yandex.practicum.shop.model.Item;
import ru.yandex.practicum.shop.repository.ItemRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final ItemRepository itemRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "item", key = "#sessionId")
    public Mono<Item> getItemById(String sessionId, Long id) {
        log.info("Получаем продукт из БД id:{} sessionId:{}", id, sessionId);
        return itemRepository.findById(id);
    }
}
