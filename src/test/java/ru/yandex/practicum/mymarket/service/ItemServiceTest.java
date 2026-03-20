package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@SpringBootTest
class ItemServiceTest {

    @Autowired
    private ItemService itemService;

    @MockitoBean
    private ItemRepository itemRepository;

    @MockitoBean
    private CartService cartService;

    private static final String SESSION_ID = "test-session";

    private final Item item1 = new Item(1L, "Товар 1", "Описание 1", "_", 100L);
    private final Item item2 = new Item(2L, "Товар 2", "Описание 2", "_", 200L);
    private final Item item3 = new Item(3L, "Товар 3", "Описание 3", "_", 150L);

    private final ItemDto itemDto1 = new ItemDto(1L, "Товар 1", "Описание 1", "_", 100L, 0);
    private final ItemDto itemDto2 = new ItemDto(2L, "Товар 2", "Описание 2", "_", 200L, 0);
    private final ItemDto itemDto3 = new ItemDto(3L, "Товар 3", "Описание 3", "_", 150L, 0);

    @Test
    void test_getAllItems_DefaultParams() {
        List<Item> items = List.of(item1, item2, item3);
        Pageable pageable = PageRequest.of(0, 5);

        when(itemRepository.findAllBy(eq(pageable)))
                .thenReturn(Flux.fromIterable(items));
        when(itemRepository.count())
                .thenReturn(Mono.just(3L));
        when(cartService.getItemCount(eq(SESSION_ID), anyLong()))
                .thenReturn(Mono.just(1));

        Mono<Page<ItemDto>> result = itemService.getAllItems(null, "NO", 1, 5, SESSION_ID);

        StepVerifier.create(result)
                .assertNext(page -> {
                    assertThat(page).isNotNull();
                    assertThat(page.getContent()).hasSize(3);
                    assertThat(page.getTotalElements()).isEqualTo(3);
                    assertThat(page.getNumber()).isEqualTo(0);
                    assertThat(page.getSize()).isEqualTo(5);

                    List<ItemDto> content = page.getContent();
                    assertThat(content.get(0).getTitle()).isEqualTo("Товар 1");
                    assertThat(content.get(1).getPrice()).isEqualTo(200L);
                    assertThat(content.get(2).getCount()).isEqualTo(1);
                })
                .verifyComplete();

        verify(itemRepository, times(1)).findAllBy(pageable);
        verify(itemRepository, times(1)).count();
        verify(cartService, times(3)).getItemCount(anyString(), anyLong());
    }

    @Test
    void test_getAllItems_WithSearch() {
        String search = "товар";
        List<Item> items = List.of(item1, item2);
        Pageable pageable = PageRequest.of(0, 5);

        when(itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                eq(search), eq(search), eq(pageable)))
                .thenReturn(Flux.fromIterable(items));
        when(itemRepository.countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                eq(search), eq(search)))
                .thenReturn(Mono.just(2L));
        when(cartService.getItemCount(eq(SESSION_ID), anyLong()))
                .thenReturn(Mono.just(2));
        Mono<Page<ItemDto>> result = itemService.getAllItems(search, "NO", 1, 5, SESSION_ID);

        StepVerifier.create(result)
                .assertNext(page -> {
                    assertThat(page).isNotNull();
                    assertThat(page.getContent()).hasSize(2);
                    assertThat(page.getTotalElements()).isEqualTo(2);

                    List<ItemDto> content = page.getContent();
                    assertThat(content.get(0).getCount()).isEqualTo(2);
                    assertThat(content.get(1).getCount()).isEqualTo(2);
                })
                .verifyComplete();

        verify(itemRepository, times(1))
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search, pageable);
        verify(itemRepository, times(1))
                .countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search);
    }

    @Test
    void test_getAllItems_WithSortByPrice() {
        List<Item> items = List.of(item1, item3, item2); // отсортировано по цене: 100, 150, 200
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "price"));

        when(itemRepository.findAllBy(eq(pageable)))
                .thenReturn(Flux.fromIterable(items));
        when(itemRepository.count())
                .thenReturn(Mono.just(3L));
        when(cartService.getItemCount(eq(SESSION_ID), anyLong()))
                .thenReturn(Mono.just(1));

        Mono<Page<ItemDto>> result = itemService.getAllItems(null, "PRICE", 1, 5, SESSION_ID);

        StepVerifier.create(result)
                .assertNext(page -> {
                    assertThat(page).isNotNull();
                    List<ItemDto> content = page.getContent();
                    assertThat(content).hasSize(3);
                    assertThat(content.get(0).getPrice()).isEqualTo(100L);
                    assertThat(content.get(1).getPrice()).isEqualTo(150L);
                    assertThat(content.get(2).getPrice()).isEqualTo(200L);
                })
                .verifyComplete();
    }

    @Test
    void test_getItemById_Success() {
        when(itemRepository.findById(eq(1L)))
                .thenReturn(Mono.just(item1));
        when(cartService.getItemCount(eq(SESSION_ID), eq(1L)))
                .thenReturn(Mono.just(5));
        Mono<ItemDto> result = itemService.getItemById(SESSION_ID, 1L);

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertThat(dto).isNotNull();
                    assertThat(dto.getId()).isEqualTo(1L);
                    assertThat(dto.getTitle()).isEqualTo("Товар 1");
                    assertThat(dto.getPrice()).isEqualTo(100L);
                    assertThat(dto.getCount()).isEqualTo(5);
                })
                .verifyComplete();

        verify(itemRepository, times(1)).findById(1L);
        verify(cartService, times(1)).getItemCount(SESSION_ID, 1L);
    }

    @Test
    void test_getItemById_NotFound() {
        when(itemRepository.findById(eq(999L)))
                .thenReturn(Mono.empty());

        Mono<ItemDto> result = itemService.getItemById(SESSION_ID, 999L);

        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();

        verify(itemRepository, times(1)).findById(999L);
        verify(cartService, never()).getItemCount(anyString(), anyLong()); // не должен вызываться
    }

    @Test
    void test_getItemByIdForOrder_Success() {
        when(itemRepository.findById(eq(2L)))
                .thenReturn(Mono.just(item2));

        Mono<ItemDto> result = itemService.getItemByIdForOrder(2L);

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertThat(dto).isNotNull();
                    assertThat(dto.getId()).isEqualTo(2L);
                    assertThat(dto.getTitle()).isEqualTo("Товар 2");
                    assertThat(dto.getPrice()).isEqualTo(200L);
                })
                .verifyComplete();

        verify(itemRepository, times(1)).findById(2L);
    }

    @Test
    void test_getItemByIdForOrder_NotFound() {
        when(itemRepository.findById(eq(999L)))
                .thenReturn(Mono.empty());
        Mono<ItemDto> result = itemService.getItemByIdForOrder(999L);

        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();

        verify(itemRepository, times(1)).findById(999L);
    }

    @Test
    void test_getAllItems_WithSortByAlpha() {
        List<Item> items = List.of(item1, item3, item2); // отсортировано по названию: "Товар 1", "Товар 3", "Товар 2"
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "title"));

        when(itemRepository.findAllBy(eq(pageable)))
                .thenReturn(Flux.fromIterable(items));
        when(itemRepository.count())
                .thenReturn(Mono.just(3L));
        when(cartService.getItemCount(eq(SESSION_ID), anyLong()))
                .thenReturn(Mono.just(1));

        Mono<Page<ItemDto>> result = itemService.getAllItems(null, "ALPHA", 1, 5, SESSION_ID);

        StepVerifier.create(result)
                .assertNext(page -> {
                    assertThat(page).isNotNull();
                    List<ItemDto> content = page.getContent();
                    assertThat(content).hasSize(3);
                    assertThat(content.get(0).getTitle()).isEqualTo("Товар 1");
                    assertThat(content.get(1).getTitle()).isEqualTo("Товар 3");
                    assertThat(content.get(2).getTitle()).isEqualTo("Товар 2");
                })
                .verifyComplete();
    }

    @Test
    void test_getAllItems_EmptyResult() {
        Pageable pageable = PageRequest.of(0, 5);

        when(itemRepository.findAllBy(eq(pageable)))
                .thenReturn(Flux.empty());
        when(itemRepository.count())
                .thenReturn(Mono.just(0L));

        Mono<Page<ItemDto>> result = itemService.getAllItems(null, "NO", 1, 5, SESSION_ID);

        StepVerifier.create(result)
                .assertNext(page -> {
                    assertThat(page).isNotNull();
                    assertThat(page.getContent()).isEmpty();
                    assertThat(page.getTotalElements()).isEqualTo(0);
                    assertThat(page.getNumber()).isEqualTo(0);
                    assertThat(page.getSize()).isEqualTo(5);
                })
                .verifyComplete();
    }

    @Test
    void test_setCount_Success() {
        when(cartService.getItemCount(eq(SESSION_ID), eq(1L)))
                .thenReturn(Mono.just(7));

        Mono<ItemDto> result = itemService.setCount(SESSION_ID, itemDto1);

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertThat(dto).isNotNull();
                    assertThat(dto.getId()).isEqualTo(1L);
                    assertThat(dto.getCount()).isEqualTo(7);
                })
                .verifyComplete();

        verify(cartService, times(1)).getItemCount(SESSION_ID, 1L);
    }

    @Test
    void test_setCount_CartServiceReturnsZero() {
        when(cartService.getItemCount(eq(SESSION_ID), eq(2L)))
                .thenReturn(Mono.just(0));

        Mono<ItemDto> result = itemService.setCount(SESSION_ID, itemDto2);

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertThat(dto).isNotNull();
                    assertThat(dto.getId()).isEqualTo(2L);
                    assertThat(dto.getCount()).isEqualTo(0);
                })
                .verifyComplete();

        verify(cartService, times(1)).getItemCount(SESSION_ID, 2L);
    }

    @Test
    void test_getAllItems_WithSearchAndSort() {
        String search = "товар";
        List<Item> items = List.of(item1, item3); // результаты поиска
        Pageable pageable = PageRequest.of(
                0,
                5,
                Sort.by(Sort.Direction.ASC, "price")
        );

        when(itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                eq(search), eq(search), eq(pageable)))
                .thenReturn(Flux.fromIterable(items));
        when(itemRepository.countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                eq(search), eq(search)))
                .thenReturn(Mono.just(2L));
        when(cartService.getItemCount(eq(SESSION_ID), anyLong()))
                .thenReturn(Mono.just(3));

        Mono<Page<ItemDto>> result = itemService.getAllItems(search, "PRICE", 1, 5, SESSION_ID);

        StepVerifier.create(result)
                .assertNext(page -> {
                    assertThat(page).isNotNull();
                    assertThat(page.getContent()).hasSize(2);
                    assertThat(page.getTotalElements()).isEqualTo(2);

                    List<ItemDto> content = page.getContent();
                    assertThat(content.get(0).getPrice()).isEqualTo(100L); // Товар 1
                    assertThat(content.get(1).getPrice()).isEqualTo(150L); // Товар 3
                    assertThat(content.get(0).getCount()).isEqualTo(3);
                    assertThat(content.get(1).getCount()).isEqualTo(3);
                })
                .verifyComplete();

        verify(itemRepository, times(1))
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search, pageable);
        verify(itemRepository, times(1))
                .countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search);
    }
}