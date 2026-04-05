package ru.yandex.practicum.shop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.dto.ItemDto;
import ru.yandex.practicum.shop.model.Item;
import ru.yandex.practicum.shop.repository.ItemRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final CartService cartService;
    private final CacheService cacheService;

    @Transactional(readOnly = true)
    public Mono<Page<ItemDto>> getAllItems(
            String search,
            String sort,
            int pageNumber,
            int pageSize,
            String sessionId
    ) {
        Sort sortOrder = Sort.unsorted();
        if ("ALPHA".equals(sort)) {
            sortOrder = Sort.by(Sort.Direction.ASC, "title");
        } else if ("PRICE".equals(sort)) {
            sortOrder = Sort.by(Sort.Direction.ASC, "price");
        }
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sortOrder);

        Mono<List<ItemDto>> items;
        Mono<Long> totalCount;

        if (search != null && !search.isEmpty()) {
            items = itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search, pageable)
                    .map(this::toDto)
                    .collectList();

            totalCount = itemRepository.countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search);
        } else {
            items = itemRepository.findAllBy(pageable)
                    .map(this::toDto)
                    .collectList();

            totalCount = itemRepository.count();
        }

        return items
                .zipWith(totalCount)
                .flatMap(tuple -> {
                    List<ItemDto> itemsDto = tuple.getT1();
                    long total = tuple.getT2();
                    return Flux.fromIterable(itemsDto)
                            .flatMap(item -> setCount(sessionId, item))
                            .collectList()
                            .map(updatedItems -> new PageImpl<>(updatedItems, pageable, total));
                });
    }

    @Transactional(readOnly = true)
    public Mono<ItemDto> getItemById(String sessionId, Long id) {
        return cacheService.getItemById(sessionId, id)
                .map(this::toDto)
                .flatMap(dto -> setCount(sessionId, dto));
    }


    Mono<ItemDto> setCount(String sessionId, ItemDto dto) {
        return cartService.getItemCount(sessionId, dto.getId())
                .map(count -> {
                    dto.setCount(count);
                    return dto;
                });
    }

    public Mono<ItemDto> getItemByIdForOrder(Long itemId) {
        return itemRepository.findById(itemId)
                .map(this::toDto);
    }

    private ItemDto toDto(Item item) {
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setTitle(item.getTitle());
        dto.setDescription(item.getDescription());
        dto.setImgPath(item.getImgPath());
        dto.setPrice(item.getPrice());
        return dto;
    }
}