package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.OrderDto;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.repository.OrderRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = OrderService.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
class OrderServiceTest {

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    private ItemDto itemDto1, itemDto2;
    private Order order;

    @BeforeEach
    void setUp() {

        itemDto1 = new ItemDto();
        itemDto1.setId(1L);
        itemDto1.setTitle("Apple");
        itemDto1.setPrice(100L);
        itemDto1.setCount(2);

        itemDto2 = new ItemDto();
        itemDto2.setId(2L);
        itemDto2.setTitle("Banana");
        itemDto2.setPrice(50L);
        itemDto2.setCount(3);

        OrderItem orderItem1 = new OrderItem();
        Item item1 = new Item();
        item1.setId(1L);
        item1.setTitle("Apple");
        item1.setPrice(100L);
        orderItem1.setItem(item1);
        orderItem1.setPrice(100L);
        orderItem1.setCount(2);

        OrderItem orderItem2 = new OrderItem();
        Item item2 = new Item();
        item2.setId(2L);
        item2.setTitle("Banana");
        item2.setPrice(50L);
        orderItem2.setItem(item2);
        orderItem2.setPrice(50L);
        orderItem2.setCount(3);

        order = new Order();
        order.setId(100L);
        order.setItems(Arrays.asList(orderItem1, orderItem2));
        order.setTotalSum(350L);  // 2×100 + 3×50
    }

    @Test
    void createOrder_shouldCreateOrderAndClearCart() {
        when(cartService.getCartItems()).thenReturn(Arrays.asList(itemDto1, itemDto2));
        when(cartService.getTotalPrice()).thenReturn(350L);

        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDto result = orderService.createOrder();

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getTotalSum()).isEqualTo(350L);
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().getFirst().getId()).isEqualTo(1L);
        assertThat(result.getItems().getFirst().getTitle()).isEqualTo("Apple");
        assertThat(result.getItems().getFirst().getPrice()).isEqualTo(100L);
        assertThat(result.getItems().getFirst().getCount()).isEqualTo(2);

        assertThat(result.getItems().get(1).getId()).isEqualTo(2L);
        assertThat(result.getItems().get(1).getTitle()).isEqualTo("Banana");
        assertThat(result.getItems().get(1).getPrice()).isEqualTo(50L);
        assertThat(result.getItems().get(1).getCount()).isEqualTo(3);

        verify(orderRepository).save(any(Order.class));

        verify(cartService).removeFromCart(1L);
        verify(cartService).removeFromCart(2L);
    }

    @Test
    void createOrder_whenCartEmpty_shouldThrowException() {
        when(cartService.getCartItems()).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.createOrder())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cart is empty");

        verify(orderRepository, never()).save(any());
        verify(cartService, never()).removeFromCart(anyLong());
    }

    @Test
    void getAllOrders_shouldReturnAllOrders() {
        when(orderRepository.findAll()).thenReturn(Arrays.asList(order));

        List<OrderDto> result = orderService.getAllOrders();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(100L);
        assertThat(result.getFirst().getTotalSum()).isEqualTo(350L);
        assertThat(result.getFirst().getItems()).hasSize(2);
    }

    @Test
    void getAllOrders_emptyList_shouldReturnEmptyList() {
        when(orderRepository.findAll()).thenReturn(List.of());

        List<OrderDto> result = orderService.getAllOrders();

        assertThat(result).isEmpty();
    }

     @Test
    void getOrderById_shouldReturnOrderDto() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        OrderDto result = orderService.getOrderById(100L);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getTotalSum()).isEqualTo(350L);
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().getFirst().getId()).isEqualTo(1L);
        assertThat(result.getItems().get(1).getId()).isEqualTo(2L);
    }

    @Test
    void getOrderById_notFound_shouldThrowException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    void toDto_shouldMapAllFieldsCorrectly() {
        OrderDto dto = orderService.toDto(order);

        assertThat(dto.getId()).isEqualTo(order.getId());
        assertThat(dto.getTotalSum()).isEqualTo(order.getTotalSum());
        assertThat(dto.getItems()).hasSize(2);

        assertThat(dto.getItems().getFirst().getId()).isEqualTo(order.getItems().getFirst().getItem().getId());
        assertThat(dto.getItems().getFirst().getTitle()).isEqualTo(order.getItems().getFirst().getItem().getTitle());
        assertThat(dto.getItems().getFirst().getPrice()).isEqualTo(order.getItems().getFirst().getPrice());
        assertThat(dto.getItems().getFirst().getCount()).isEqualTo(order.getItems().getFirst().getCount());
    }
}
