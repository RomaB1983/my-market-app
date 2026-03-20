package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.OrderDto;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.repository.OrderItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private OrderItemRepository orderItemRepository;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private ItemService itemService;

    private static final String SESSION_ID = "test-session";

    // Вспомогательные данные
    private final ItemDto itemDto1 = new ItemDto(1L, "Товар 1", "Описание 1", "_", 100L, 2);
    private final ItemDto itemDto2 = new ItemDto(2L, "Товар 2", "Описание 2", "_", 200L, 1);

    private final Order order = new Order();

    {
        order.setId(1L);
        order.setSessionId(SESSION_ID);
        order.setTotalSum(400L);

        List<OrderItem> items = List.of(
                new OrderItem(1L,1L, 1L, 2, 100L),
                new OrderItem(2L,2L, 1L, 1, 200L)
        );
        order.setItems(items);
    }

    private final OrderDto orderDto = new OrderDto(
            1L,
            List.of(itemDto1, itemDto2),
            400L
    );

    @Test
    void test_createOrder_Success() {
        // Подготовка данных
        List<ItemDto> cartItems = List.of(itemDto1, itemDto2);

        when(cartService.removeItem(eq(SESSION_ID), anyLong()))
                .thenReturn(Mono.empty());
        when(cartService.getCartItems(eq(SESSION_ID)))
                .thenReturn(Mono.just(cartItems));
        when(orderRepository.save(any(Order.class)))
                .thenReturn(Mono.just(order));
        when(orderItemRepository.saveAll(anyList()))
                .thenReturn(Flux.fromIterable(order.getItems()));
        Mono<OrderDto> result = orderService.createOrder(SESSION_ID);

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertThat(dto).isNotNull();
                    assertThat(dto.getId()).isEqualTo(1L);
                    assertThat(dto.getTotalSum()).isEqualTo(400L);
                    assertThat(dto.getItems()).hasSize(2);
                })
                .verifyComplete();

        verify(cartService, times(1)).getCartItems(SESSION_ID);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderItemRepository, times(1)).saveAll(anyList());
        verify(cartService, times(2)).removeItem(eq(SESSION_ID), anyLong());
    }

    @Test
    void test_createOrder_EmptyCart() {
        when(cartService.getCartItems(eq(SESSION_ID)))
                .thenReturn(Mono.just(List.of()));

        Mono<OrderDto> result = orderService.createOrder(SESSION_ID);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable.getMessage().equals("Cart is empty"))
                .verify();

        verify(cartService, times(1)).getCartItems(SESSION_ID);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void test_getAllOrders_Success() {
        when(orderRepository.findBySessionId(eq(SESSION_ID)))
                .thenReturn(Flux.just(order));
        when(orderItemRepository.findByOrderId(order.getId()))
                .thenReturn(Flux.fromIterable(order.getItems()));
        when(itemService.getItemByIdForOrder(eq(1L)))
                .thenReturn(Mono.just(itemDto1));
        when(itemService.getItemByIdForOrder(eq(2L)))
                .thenReturn(Mono.just(itemDto2));

        Mono<List<OrderDto>> result = orderService.getAllOrders(SESSION_ID);

        StepVerifier.create(result)
                .assertNext(orders -> {
                    assertThat(orders).hasSize(1);
                    OrderDto dto = orders.getFirst();
                    assertThat(dto.getId()).isEqualTo(1L);
                    assertThat(dto.getTotalSum()).isEqualTo(400L);
                    assertThat(dto.getItems()).hasSize(2);
                })
                .verifyComplete();

        verify(orderRepository, times(1)).findBySessionId(SESSION_ID);
        verify(itemService, times(2)).getItemByIdForOrder(anyLong());
    }

    @Test
    void test_getAllOrders_NoOrders() {
        when(orderRepository.findBySessionId(eq(SESSION_ID)))
                .thenReturn(Flux.empty());

        Mono<List<OrderDto>> result = orderService.getAllOrders(SESSION_ID);

        StepVerifier.create(result)
                .assertNext(orders -> {
                    assertThat(orders).isEmpty();
                })
                .verifyComplete();

        verify(orderRepository, times(1)).findBySessionId(SESSION_ID);
    }

    @Test
    void test_getOrderById_Success() {
        when(orderRepository.findByIdAndSessionId(eq(1L), eq(SESSION_ID)))
                .thenReturn(Mono.just(order));
        when(orderItemRepository.findByOrderId(eq(order.getId())))
                .thenReturn(Flux.fromIterable(order.getItems()));
        when(itemService.getItemByIdForOrder(eq(1L)))
                .thenReturn(Mono.just(itemDto1));
        when(itemService.getItemByIdForOrder(eq(2L)))
                .thenReturn(Mono.just(itemDto2));

        Mono<OrderDto> result = orderService.getOrderById(SESSION_ID, 1L);

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertThat(dto).isNotNull();
                    assertThat(dto.getId()).isEqualTo(1L);
                    assertThat(dto.getTotalSum()).isEqualTo(400L);
                    assertThat(dto.getItems()).hasSize(2);
                })
                .verifyComplete();

        verify(orderRepository, times(1)).findByIdAndSessionId(1L, SESSION_ID);
        verify(itemService, times(2)).getItemByIdForOrder(anyLong());
    }

    @Test
    void test_getOrderById_NotFound() {
        when(orderRepository.findByIdAndSessionId(eq(999L), eq(SESSION_ID)))
                .thenReturn(Mono.empty());

        Mono<OrderDto> result = orderService.getOrderById(SESSION_ID, 999L);

        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();

        verify(orderRepository, times(1)).findByIdAndSessionId(999L, SESSION_ID);
        verify(itemService, never()).getItemByIdForOrder(anyLong());
    }

    void test_getItems_Success() {
        OrderItem orderItem1 = new OrderItem();
        orderItem1.setId(1L);
        orderItem1.setItemId(1L);
        orderItem1.setCount(2);
        orderItem1.setPrice(100L);

        OrderItem orderItem2 = new OrderItem();
        orderItem2.setId(2L);
        orderItem2.setItemId(2L);
        orderItem2.setCount(1);
        orderItem2.setPrice(200L);

        when(orderItemRepository.findByOrderId(eq(1L)))
                .thenReturn(Flux.fromIterable(List.of(orderItem1, orderItem2)));
        when(itemService.getItemByIdForOrder(eq(1L)))
                .thenReturn(Mono.just(itemDto1));
        when(itemService.getItemByIdForOrder(eq(2L)))
                .thenReturn(Mono.just(itemDto2));

        Mono<List<ItemDto>> result = orderService.getItems(1L);

        StepVerifier.create(result)
                .assertNext(items -> {
                    assertThat(items).hasSize(2);
                    ItemDto item1 = items.get(0);
                    assertThat(item1.getId()).isEqualTo(1L);
                    assertThat(item1.getCount()).isEqualTo(2);

                    ItemDto item2 = items.get(1);
                    assertThat(item2.getId()).isEqualTo(2L);
                    assertThat(item2.getCount()).isEqualTo(1);
                })
                .verifyComplete();

        verify(orderItemRepository, times(1)).findByOrderId(1L);
        verify(itemService, times(2)).getItemByIdForOrder(anyLong());
    }

    @Test
    void test_getItems_NoItems() {
        when(orderItemRepository.findByOrderId(eq(999L)))
                .thenReturn(Flux.empty());

        Mono<List<ItemDto>> result = orderService.getItems(999L);

        StepVerifier.create(result)
                .assertNext(items -> {
                    assertThat(items).isEmpty();
                })
                .verifyComplete();

        verify(orderItemRepository, times(1)).findByOrderId(999L);
        verify(itemService, never()).getItemByIdForOrder(anyLong());
    }

    @Test
    void test_toDto_Success() {
        List<ItemDto> cartItems = List.of(itemDto1, itemDto2);

        OrderDto dto = orderService.toDto(order, cartItems);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTotalSum()).isEqualTo(400L);
        assertThat(dto.getItems()).hasSize(2);

        ItemDto item1 = dto.getItems().get(0);
        assertThat(item1.getId()).isEqualTo(1L);
        assertThat(item1.getTitle()).isEqualTo("Товар 1");
        assertThat(item1.getPrice()).isEqualTo(100L);
    }

    @Test
    void test_createOrder_RepositorySaveError() {
        List<ItemDto> cartItems = List.of(itemDto1);

        when(cartService.getCartItems(eq(SESSION_ID)))
                .thenReturn(Mono.just(cartItems));
        when(orderRepository.save(any(Order.class)))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        Mono<OrderDto> result = orderService.createOrder(SESSION_ID);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable.getMessage().equals("Database error"))
                .verify();

        verify(cartService, times(1)).getCartItems(SESSION_ID);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderItemRepository, never()).saveAll(anyList());
        verify(cartService, never()).removeItem(anyString(), anyLong());
    }

    @Test
    void test_getAllOrders_RepositoryError() {
        when(orderRepository.findBySessionId(eq(SESSION_ID)))
                .thenReturn(Flux.error(new RuntimeException("Database error")));

        Mono<List<OrderDto>> result = orderService.getAllOrders(SESSION_ID);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable.getMessage().equals("Database error"))
                .verify();

        verify(orderRepository, times(1)).findBySessionId(SESSION_ID);
    }
}