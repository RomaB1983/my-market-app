package ru.yandex.practicum.shop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.client.model.PaymentStatus;
import ru.yandex.practicum.shop.dto.ItemDto;
import ru.yandex.practicum.shop.dto.OrderDto;
import ru.yandex.practicum.shop.model.Order;
import ru.yandex.practicum.shop.model.OrderItem;
import ru.yandex.practicum.shop.repository.OrderItemRepository;
import ru.yandex.practicum.shop.repository.OrderRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;
    private final ItemService itemService;
    private final PaymentService paymentService;

    @Transactional
    public Mono<OrderDto> createOrder(String username) {
        return cartService.getCartItems(username)
                .flatMap(cartItems -> {
                    if (cartItems.isEmpty()) {
                        return Mono.error(new RuntimeException("Cart is empty"));
                    }

                    Long totalSum = getTotalSum(cartItems);
                    List<OrderItem> orderItems = cartItems.stream()
                            .map(this::toOrderItem)
                            .collect(Collectors.toList());

                    return paymentService.createPayment(username, totalSum)
                            .flatMap(paymentStatus -> {
                                if (paymentStatus.equals(PaymentStatus.ERROR)) {
                                    return Mono.error(new IllegalStateException(
                                            "Не удалось выполнить платеж. Недостаточно средств"));
                                }

                                Order order = new Order();
                                order.setUsername(username);
                                order.setItems(orderItems);
                                order.setTotalSum(totalSum);

                                return orderRepository.save(order)
                                        .flatMap(savedOrder -> {
                                            List<OrderItem> itemsWithOrderId = savedOrder.getItems().stream()
                                                    .peek(item -> item.setOrderId(savedOrder.getId()))
                                                    .collect(Collectors.toList());

                                            return orderItemRepository.saveAll(itemsWithOrderId)
                                                    .collectList()
                                                    .thenReturn(savedOrder);
                                        })
                                        .map(savedOrder -> toDto(savedOrder, cartItems))
                                        .flatMap(dto -> cartService.removeAllItems(username)
                                                .thenReturn(dto));
                            });
                })
                .doOnSuccess(orderDto -> log.info("Order created successfully: {}", orderDto.getId()))
                .doOnError(error -> log.error("Failed to create order for session: {}", username, error));
    }

    protected Long getTotalSum(List<ItemDto> cartItems) {
        return cartItems.stream()
                .mapToLong(item -> item.getPrice() * item.getCount())
                .sum();
    }

    private OrderItem toOrderItem(ItemDto item) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItemId(item.getId());
        orderItem.setCount(item.getCount());
        orderItem.setPrice(item.getPrice());
        return orderItem;
    }

    public Mono<List<OrderDto>> getAllOrders(String username) {
        return orderRepository.findByUsername(username)
                .flatMap(order -> getItems(order.getId())
                        .map(items -> toDto(order, items)))
                .collectList();
    }

    public Mono<OrderDto> getOrderById(String username, Long id) {
        return orderRepository.findByIdAndUsername(id, username)
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