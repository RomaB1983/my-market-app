package ru.yandex.practicum.mymarket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.OrderDto;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.repository.OrderRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;

    @Transactional
    public OrderDto createOrder() {
        List<ItemDto> cartItems = cartService.getCartItems();
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = new Order();
        order.setItems(cartItems.stream()
                .map(itemDto -> {
                    OrderItem orderItem = new OrderItem();
                    Item item = new Item();
                    item.setId(itemDto.getId());
                    item.setTitle(itemDto.getTitle());
                    orderItem.setItem(item);
                    // orderItem.setId(itemDto.getId());
                    orderItem.setPrice(itemDto.getPrice());
                    orderItem.setCount(itemDto.getCount());
                    orderItem.setPrice(itemDto.getPrice());
                    return orderItem;
                })
                .collect(Collectors.toList()));
        order.setTotalSum(cartService.getTotalPrice());

        Order savedOrder = orderRepository.save(order);

        // Очищаем корзину после оформления заказа
        cartService.getCartItems().forEach(item -> cartService.removeFromCart(item.getId()));

        return toDto(savedOrder);
    }

    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return toDto(order);
    }

    protected OrderDto toDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setItems(order.getItems().stream()
                .map(item -> {
                    ItemDto itemDto = new ItemDto();
                    itemDto.setId(item.getItem().getId());
                    itemDto.setTitle(item.getItem().getTitle());
                    itemDto.setPrice(item.getItem().getPrice());
                    itemDto.setCount(item.getCount());
                    return itemDto;
                })
                .collect(Collectors.toList()));
        dto.setTotalSum(order.getTotalSum());
        return dto;
    }
}
