package ru.yandex.practicum.mymarket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final ItemRepository itemRepository;
    private final Map<Long, Integer> cart = new HashMap<>(); // id товара → количество

    public void addToCart(Long itemId, int quantity) {
        cart.put(itemId, cart.getOrDefault(itemId, 0) + quantity);
    }

    public void removeFromCart(Long itemId) {
        cart.remove(itemId);
    }

    public void clearCart() {
        cart.clear();
    }

    ;

    public void updateQuantity(Long itemId, int newQuantity) {
        if (newQuantity <= 0) {
            cart.remove(itemId);
        } else {
            cart.put(itemId, newQuantity);
        }
    }

    public List<ItemDto> getCartItems() {
        return cart.entrySet().stream()
                .map(entry -> {
                    Item item = itemRepository.findById(entry.getKey())
                            .orElseThrow(() -> new RuntimeException("Item not found"));
                    ItemDto dto = toDto(item);
                    dto.setCount(entry.getValue());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public long getTotalPrice() {
        return getCartItems().stream()
                .mapToLong(item -> item.getPrice() * item.getCount())
                .reduce(0L, Long::sum);
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

    public int getItemCount(Long itemId) {
        return cart.getOrDefault(itemId, 0);
    }
}
