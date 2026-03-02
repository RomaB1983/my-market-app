package ru.yandex.practicum.mymarket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final CartService cartService;

    @Transactional(readOnly = true)
    public Page<ItemDto> getAllItems(String search, String sort, int pageNumber, int pageSize) {
        Sort sortOrder = Sort.unsorted();
        if ("ALPHA".equals(sort)) {
            sortOrder = Sort.by(Sort.Direction.ASC, "title");
        } else if ("PRICE".equals(sort)) {
            sortOrder = Sort.by(Sort.Direction.ASC, "price");
        }
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sortOrder);

        Page<Item> items;

        // Поиск по названию/описанию
        if (search != null && !search.isEmpty()) {
            items = itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search, pageable);
        } else {
            items = itemRepository.findAll(pageable);
        }

        List<ItemDto> dtoList = items.getContent().stream()
                .map(this::toDto)
                .toList();

        return new PageImpl<>(dtoList, items.getPageable(), items.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ItemDto getItemById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        return toDto(item);
    }

    private ItemDto toDto(Item item) {
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setTitle(item.getTitle());
        dto.setDescription(item.getDescription());
        dto.setImgPath(item.getImgPath());
        dto.setPrice(item.getPrice());
        dto.setCount(cartService.getItemCount(item.getId())); // уже может быть что-то в корзине
        return dto;
    }
}

