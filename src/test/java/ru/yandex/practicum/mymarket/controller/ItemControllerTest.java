package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.Params;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderRepository;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.ItemService;

import java.util.List;

import static org.mockito.Mockito.*;

@WebFluxTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private OrderItemRepository orderItemRepository;

    @MockitoBean
    private ItemRepository itemRepository;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private CartItemRepository cartItemRepository;

    private final ItemDto item1 = new ItemDto(1L, "Товар 1", "Описание 1", "_", 100L, 2);
    private final ItemDto item2 = new ItemDto(2L, "Товар 2", "Описание 2", "_", 200L, 3);
    private final ItemDto item3 = new ItemDto(3L, "Товар 3", "Описание 3", "_", 150L, 1);

    @Test
    void test_getAllItems_DefaultParams() {
        List<ItemDto> items = List.of(item1, item2, item3);
        Pageable pageable = PageRequest.of(0, 5, Sort.unsorted());
        Page<ItemDto> page = new PageImpl<>(items, pageable, items.size());

        when(itemService.getAllItems(isNull(), eq("NO"), eq(1), eq(5), anyString()))
                .thenReturn(Mono.just(page));
        webTestClient.get()
                .uri("/items")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    Assertions.assertTrue(html.contains("Товар 1"), "HTML должен содержать товар 1");
                    Assertions.assertTrue(html.contains("Товар 2"), "HTML должен содержать товар 2");
                    Assertions.assertTrue(html.contains("Товар 3"), "HTML должен содержать товар 3");
                });

        verify(itemService, times(1)).getAllItems(isNull(), eq("NO"), eq(1), eq(5), anyString());
    }

    @Test
    void test_getAllItems_WithSearchAndSort() {
        List<ItemDto> items = List.of(item1);
        Pageable pageable = PageRequest.of(1, 10, Sort.unsorted());
        Page<ItemDto> page = new PageImpl<>(items, pageable, items.size());

        when(itemService.getAllItems(eq("товар"), eq("PRICE"), eq(2), eq(10), anyString()))
                .thenReturn(Mono.just(page));
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/items")
                        .queryParam("search", "товар")
                        .queryParam("sort", "PRICE")
                        .queryParam("pageNumber", "2")
                        .queryParam("pageSize", "10")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    Assertions.assertTrue(html.contains("Товар 1"));
                    Assertions.assertTrue(html.contains("name=\"search\" value=\"товар\""));
                    Assertions.assertTrue(html.contains("name=\"sort\" value=\"PRICE\""));
                });

        verify(itemService).getAllItems(eq("товар"), eq("PRICE"), eq(2), eq(10), anyString());
    }

    @Test
    void test_getItem_Success() {
        when(itemService.getItemById(anyString(), eq(1L)))
                .thenReturn(Mono.just(item1));
        webTestClient.get()
                .uri("/items/1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    Assertions.assertTrue(html.contains("Товар 1"));
                    Assertions.assertTrue(html.contains("Описание 1"));
                    Assertions.assertTrue(html.contains("100"));
                });

        verify(itemService).getItemById(anyString(), eq(1L));
    }

    @Test
    void test_updateCart_Success() {
        Params params = new Params();
        params.setId(1L);
        params.setAction("PLUS");
        params.setSearch("товар");
        params.setSort("PRICE");
        params.setPageNumber(2);
        params.setPageSize(10);

        when(cartService.updateQuantity(anyString(), eq(1L), eq("PLUS")))
                .thenReturn(Mono.empty());
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/items")
                        .queryParam("id", "1")
                        .queryParam("search", "товар")
                        .queryParam("sort", "PRICE")
                        .queryParam("pageNumber", "2")
                        .queryParam("pageSize", "10")
                        .queryParam("action", "PLUS")
                        .build())
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().location("/items?search=%D1%82%D0%BE%D0%B2%D0%B0%D1%80&sort=PRICE&pageNumber=2&pageSize=10");

        verify(cartService).updateQuantity(anyString(), eq(1L), eq("PLUS"));
    }

    @Test
    void test_updateCartItem_Success() {
        Params params = new Params();
        params.setId(2L);
        params.setAction("MINUS");

        when(cartService.updateQuantity(anyString(), eq(2L), eq("MINUS")))
                .thenReturn(Mono.empty());
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/items")
                        .queryParam("id", "2")
                        .queryParam("action", "MINUS")
                        .build())
                .exchange()

                .expectStatus().isSeeOther();

        verify(cartService).updateQuantity(anyString(), eq(2L), eq("MINUS"));
    }
}