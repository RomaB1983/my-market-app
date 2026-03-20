package ru.yandex.practicum.mymarket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.OrderDto;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.repository.OrderItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;
    private final ItemService itemService;

    @Transactional
    public Mono<OrderDto> createOrder(String sessionId) {
        return cartService.getCartItems(sessionId)
                .flatMap(cartItems -> {
                    if (cartItems.isEmpty()) {
                        return Mono.error(new RuntimeException("Cart is empty"));
                    }

                    long totalSum = 0;
                    List<OrderItem> orderItems = new ArrayList<>();

                    for (ItemDto item : cartItems) {
                        OrderItem orderItem = new OrderItem();
                        orderItem.setItemId(item.getId());
                        orderItem.setCount(item.getCount());
                        orderItem.setPrice(item.getPrice());

                        totalSum += item.getPrice() * item.getCount();
                        orderItems.add(orderItem);
                    }

                    Order order = new Order();
                    order.setSessionId(sessionId);
                    order.setItems(orderItems);
                    order.setTotalSum(totalSum);

                    return orderRepository.save(order)
                            .flatMap(savedOrder -> {
                                List<OrderItem> itemsWithOrderId = savedOrder.getItems().stream()
                                        .peek(item -> item.setOrderId(savedOrder.getId()))
                                        .toList();

                                return orderItemRepository.saveAll(itemsWithOrderId)
                                        .collectList()
                                        .thenReturn(savedOrder);
                            })
                            .map(savedOrder -> toDto(savedOrder, cartItems))
                            .flatMap(dto -> {
                                return Flux.fromIterable(cartItems)
                                        .flatMap(item -> cartService.removeItem(sessionId, item.getId()))
                                        .then(Mono.just(dto));
                            });
                });
    }

    public Mono<List<OrderDto>> getAllOrders(String sessionId) {
        return orderRepository.findBySessionId(sessionId)
                .flatMap(order -> getItems(order.getId())
                        .map(items -> toDto(order, items)))
                .collectList();
    }

    public Mono<OrderDto> getOrderById(String sessionId, Long id) {
        return orderRepository.findByIdAndSessionId(id, sessionId)
                .flatMap(order -> getItems(order.getId())
                        .map(items -> toDto(order, items)));
    }

    Mono<List<ItemDto>> getItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId)
                .flatMap(orderItem ->
                        itemService.getItemByIdForOrder(orderItem.getItemId())
                                .map(itemDto -> {
                                    itemDto.setCount(orderItem.getCount());
                                    return itemDto;
                                })
                )
                .collectList();
    }

    protected OrderDto toDto(Order order, List<ItemDto> items) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setItems(items);
        dto.setTotalSum(order.getTotalSum());
        return dto;
    }
}