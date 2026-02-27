package ru.yandex.practicum.mymarket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final CartService cartService;


    public List<ItemDto> getAllItems(String search, String sort, int pageNumber, int pageSize) {
        List<Item> items;

        // Поиск по названию/описанию
        if (search != null && !search.isEmpty()) {
            items = itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search);
        } else {
            items = itemRepository.findAll();
        }

        // Сортировка
        switch (sort) {
            case "ALPHA" -> items.sort((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()));
            case "PRICE" -> items.sort(Comparator.comparingLong(Item::getPrice));
            default -> {} // NO sorting
        }

        // Пагинация
        int start = (pageNumber - 1) * pageSize;
        int end = Math.min(start + pageSize, items.size());
        List<Item> paginatedItems = items.subList(Math.max(0, start), end);

        return paginatedItems.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

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

