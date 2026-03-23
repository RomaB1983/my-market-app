package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.OrderDto;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderRepository;
import ru.yandex.practicum.mymarket.service.OrderService;

import java.util.List;

import static org.mockito.Mockito.*;

@WebFluxTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private OrderItemRepository orderItemRepository;

    @MockitoBean
    private ItemRepository itemRepository;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private CartItemRepository cartItemRepository;

    @MockitoBean
    private OrderService orderService;

    private final ItemDto item1 = new ItemDto(1L, "Товар 1", "Описание 1", "_", 100L, 2);
    private final ItemDto item2 = new ItemDto(2L, "Товар 2", "Описание 2", "_", 200L, 3);
    private final OrderDto order1 = new OrderDto(1L, List.of(item1, item2), 1000L);
    private final OrderDto order2 = new OrderDto(2L, List.of(item1, item2), 2000L);

    @Test
    void test_getAllOrders_Success() {
        // Подготовка данных
        List<OrderDto> orders = List.of(order1, order2);

        when(orderService.getAllOrders(anyString()))
                .thenReturn(Mono.just(orders));

        webTestClient.get()
                .uri("/orders")
                .exchange()
                // Проверки
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith("text/html")
                .expectBody(String.class)
                .value(html -> {
                    Assertions.assertTrue(html.contains("Товар 1"), "HTML должен содержать товар 1");
                    Assertions.assertTrue(html.contains("Товар 2"), "HTML должен содержать товар 2");

                });

        verify(orderService, times(1)).getAllOrders(anyString());
    }

    @Test
    void test_getAllOrders_Empty() {
        List<OrderDto> emptyOrders = List.of();

        when(orderService.getAllOrders(anyString()))
                .thenReturn(Mono.just(emptyOrders));

        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class);

        verify(orderService, times(1)).getAllOrders(anyString());
    }

    @Test
    void test_getOrder_Success() {
        when(orderService.getOrderById(anyString(), eq(1L)))
                .thenReturn(Mono.just(order1));

        webTestClient.get()
                .uri("/orders/1")
                .exchange()
                // Проверки
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith("text/html")
                .expectBody(String.class)
                .value(html -> {
                    Assertions.assertTrue(html.contains("Заказ №1"));
                });

        verify(orderService, times(1)).getOrderById(anyString(), eq(1L));
    }

    @Test
    void test_getOrder_WithNewOrderParam() {
        when(orderService.getOrderById(anyString(), eq(2L)))
                .thenReturn(Mono.just(order2));

        webTestClient.get()
                .uri("/orders/2?newOrder=true")
                .exchange()
                // Проверки
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    Assertions.assertTrue(html.contains("Поздравляем! Успешная покупка!"));
                });

        verify(orderService, times(1)).getOrderById(anyString(), eq(2L));
    }

    @Test
    void test_createOrder_Success() {
        OrderDto savedOrder = new OrderDto(3L, List.of(item1), 3000L);

        when(orderService.createOrder(anyString()))
                .thenReturn(Mono.just(savedOrder));

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().location("/orders/3?newOrder=true");

        verify(orderService, times(1)).createOrder(anyString());
    }

    @Test
    void test_createOrder_ServiceError() {
        when(orderService.createOrder(anyString()))
                .thenReturn(Mono.error(new RuntimeException("Ошибка создания заказа")));
        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is5xxServerError();

        verify(orderService, times(1)).createOrder(anyString());
    }
}