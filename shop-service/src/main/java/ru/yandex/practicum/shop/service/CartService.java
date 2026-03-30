package ru.yandex.practicum.shop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.dto.ItemDto;
import ru.yandex.practicum.shop.model.CartItem;
import ru.yandex.practicum.shop.model.Item;
import ru.yandex.practicum.shop.repository.CartItemRepository;
import ru.yandex.practicum.shop.repository.ItemRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final ItemRepository itemRepository;
    private final CartItemRepository cartItemRepository;

    public Mono<List<ItemDto>> getCartItems(String sessionId) {
        return cartItemRepository.findBySessionId(sessionId)
                .flatMap(item -> itemRepository.findById(item.getItemId())
                        .map(it -> {
                            ItemDto dto = toDto(it);
                            dto.setCount(item.getQuantity());
                            return dto;
                        }))
                .collectList();
    }

    public Mono<Void> modifyItem(String sessionId, Long itemId, int quantity) {
        return cartItemRepository.findBySessionIdAndItemId(sessionId, itemId)
                .flatMap(existingItem -> {
                    int newQuantity = existingItem.getQuantity() + quantity;
                    if (newQuantity > 0) {
                        existingItem.setQuantity(newQuantity);
                        return cartItemRepository.save(existingItem)
                                .thenReturn(true);
                    } else {
                        return cartItemRepository.delete(existingItem)
                                .thenReturn(true);
                    }
                })
                .switchIfEmpty(Mono.defer(() ->
                        itemRepository.findById(itemId)
                                .flatMap(item -> {
                                    CartItem newItem = new CartItem();
                                    newItem.setSessionId(sessionId);
                                    newItem.setItemId(itemId);
                                    newItem.setQuantity(quantity);
                                    return cartItemRepository.save(newItem)
                                            .thenReturn(true);
                                })))
                .then();
    }

    public Mono<Void> removeItem(String sessionId, Long itemId) {
        return cartItemRepository.findBySessionIdAndItemId(sessionId, itemId)
                .flatMap(cartItemRepository::delete);
    }


//    public Mono<Void> clearCart() {
//        return Mono.fromRunnable(cart::clear)
//                .then();
//    }

    public Mono<Void> updateQuantity(String sessionId, Long itemId, String action) {
        if (action == null) return Mono.empty();
        return Mono.just(action)
                .flatMap(act -> switch (act) {
                    case "PLUS" -> modifyItem(sessionId, itemId, 1);
                    case "MINUS" -> modifyItem(sessionId, itemId, -1);
                    case "DELETE" -> removeItem(sessionId, itemId);
                    default -> Mono.empty();
                });
    }

    public Mono<Integer> getItemCount(String sessionId, Long itemId) {
        return cartItemRepository.findBySessionIdAndItemId(sessionId, itemId)
                .map(CartItem::getQuantity)
                .switchIfEmpty(Mono.just(0));
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
