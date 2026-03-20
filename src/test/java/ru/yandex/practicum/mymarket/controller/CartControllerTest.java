package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderRepository;
import ru.yandex.practicum.mymarket.service.CartService;
import java.util.List;

import static org.mockito.Mockito.*;


@WebFluxTest(CartController.class)
class CartControllerTest {
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
    private CartService cartService;

    @Test
    void test_getCartItems_Success() throws Exception {
        List<ItemDto> mockItems = List.of(
                new ItemDto(1L, "Товар 1", "Товар 1", "_", 100L, 2)
        );

        when(cartService.getCartItems(anyString()))
                .thenReturn(Mono.just(mockItems));

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assert html.contains(" <h5 class=\"card-title\">Товар 1</h5>");
                    assert html.contains("<h2>Итого: 200 руб.</h2>");
                    assert html.contains("100 руб.");
                });
    }

    @Test
    void test_updateItemCart_Success() throws Exception {
        when(cartService.updateQuantity(anyString(), anyLong(), eq("PLUS")))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/cart/items")
                        .queryParam("action", "PLUS")
                        .queryParam("id", "1")
                        .build())
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().location("/cart/items");

        verify(cartService, times(1)).updateQuantity(anyString(), eq(1L), eq("PLUS"));
    }
}