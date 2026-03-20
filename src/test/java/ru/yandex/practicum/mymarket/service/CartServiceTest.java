package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class CartServiceTest {

    @Autowired
    private CartService cartService;

    @MockitoBean
    private ItemRepository itemRepository;

    @MockitoBean
    private CartItemRepository cartItemRepository;

    private static final String SESSION_ID = "test-session";

    private final Item item1 = new Item(1L, "Товар 1", "Описание 1", "_", 100L);
    private final Item item2 = new Item(2L, "Товар 2", "Описание 2", "_", 200L);
    private final CartItem cartItem1 = new CartItem();
    private final CartItem cartItem2 = new CartItem();

    {
        cartItem1.setId(1L);
        cartItem1.setSessionId(SESSION_ID);
        cartItem1.setItemId(1L);
        cartItem1.setQuantity(2);

        cartItem2.setId(2L);
        cartItem2.setSessionId(SESSION_ID);
        cartItem2.setItemId(2L);
        cartItem2.setQuantity(1);
    }

    private final ItemDto itemDto1 = new ItemDto(1L, "Товар 1", "Описание 1", "_", 100L, 2);
    private final ItemDto itemDto2 = new ItemDto(2L, "Товар 2", "Описание 2", "_", 200L, 1);

    @Test
    void test_getCartItems_Success() {
        when(cartItemRepository.findBySessionId(eq(SESSION_ID)))
                .thenReturn(Mono.just(cartItem1).concatWith(Mono.just(cartItem2)));
        when(itemRepository.findById(eq(1L)))
                .thenReturn(Mono.just(item1));
        when(itemRepository.findById(eq(2L)))
                .thenReturn(Mono.just(item2));

        Mono<List<ItemDto>> result = cartService.getCartItems(SESSION_ID);

        StepVerifier.create(result)
                .assertNext(items -> {
                    assertThat(items).hasSize(2);
                    assertThat(items.get(0).getId()).isEqualTo(1L);
                    assertThat(items.get(0).getCount()).isEqualTo(2);
                    assertThat(items.get(1).getId()).isEqualTo(2L);
                    assertThat(items.get(1).getCount()).isEqualTo(1);
                })
                .verifyComplete();

        verify(cartItemRepository, times(1)).findBySessionId(SESSION_ID);
        verify(itemRepository, times(2)).findById(anyLong());
    }

    @Test
    void test_getCartItems_EmptyCart() {
        when(cartItemRepository.findBySessionId(eq(SESSION_ID)))
                .thenReturn(Flux.empty());

        Mono<List<ItemDto>> result = cartService.getCartItems(SESSION_ID);

        StepVerifier.create(result)
                .assertNext(items -> {
                    assertThat(items).isEmpty();
                })
                .verifyComplete();

        verify(cartItemRepository, times(1)).findBySessionId(SESSION_ID);
        verify(itemRepository, never()).findById(anyLong());
    }

    @Test
    void test_modifyItem_AddNewItem() {
        when(cartItemRepository.findBySessionIdAndItemId(eq(SESSION_ID), eq(1L)))
                .thenReturn(Mono.empty());
        when(itemRepository.findById(eq(1L)))
                .thenReturn(Mono.just(item1));
        when(cartItemRepository.save(any(CartItem.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        Mono<Void> result = cartService.modifyItem(SESSION_ID, 1L, 3);

        StepVerifier.create(result)
                .verifyComplete();

        verify(cartItemRepository, times(1))
                .findBySessionIdAndItemId(SESSION_ID, 1L);
        verify(itemRepository, times(1)).findById(1L);
        verify(cartItemRepository, times(1)).save(argThat(cartItem ->
                cartItem.getSessionId().equals(SESSION_ID) &&
                        cartItem.getItemId().equals(1L) &&
                        cartItem.getQuantity() == 3
        ));
    }

    @Test
    void test_modifyItem_UpdateExistingItem() {
        when(cartItemRepository.findBySessionIdAndItemId(eq(SESSION_ID), eq(1L)))
                .thenReturn(Mono.just(cartItem1));
        when(cartItemRepository.save(eq(cartItem1)))
                .thenReturn(Mono.just(cartItem1));

        Mono<Void> result = cartService.modifyItem(SESSION_ID, 1L, 2);

        StepVerifier.create(result)
                .verifyComplete();

        assertThat(cartItem1.getQuantity()).isEqualTo(4);
        verify(cartItemRepository, times(1))
                .findBySessionIdAndItemId(SESSION_ID, 1L);
        verify(cartItemRepository, times(1)).save(cartItem1);
    }

    @Test
    void test_modifyItem_RemoveItemWhenQuantityZero() {
        when(cartItemRepository.findBySessionIdAndItemId(eq(SESSION_ID), eq(1L)))
                .thenReturn(Mono.just(cartItem1));
        when(cartItemRepository.delete(eq(cartItem1)))
                .thenReturn(Mono.empty());

        Mono<Void> result = cartService.modifyItem(SESSION_ID, 1L, -2);

        StepVerifier.create(result)
                .verifyComplete();

        verify(cartItemRepository, times(1))
                .findBySessionIdAndItemId(SESSION_ID, 1L);
        verify(cartItemRepository, times(1)).delete(cartItem1);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void test_removeItem_Success() {
        when(cartItemRepository.findBySessionIdAndItemId(eq(SESSION_ID), eq(1L)))
                .thenReturn(Mono.just(cartItem1));
        when(cartItemRepository.delete(eq(cartItem1)))
                .thenReturn(Mono.empty());

        Mono<Void> result = cartService.removeItem(SESSION_ID, 1L);

        StepVerifier.create(result)
                .verifyComplete();

        verify(cartItemRepository, times(1))
                .findBySessionIdAndItemId(SESSION_ID, 1L);
        verify(cartItemRepository, times(1)).delete(cartItem1);
    }

    @Test
    void test_updateQuantity_Plus() {
        when(cartItemRepository.findBySessionIdAndItemId(eq(SESSION_ID), eq(1L)))
                .thenReturn(Mono.just(cartItem1));
        when(cartItemRepository.save(eq(cartItem1)))
                .thenReturn(Mono.just(cartItem1));

        Mono<Void> result = cartService.updateQuantity(SESSION_ID, 1L, "PLUS");

        StepVerifier.create(result)
                .verifyComplete();

        verify(cartItemRepository, times(1))
                .findBySessionIdAndItemId(SESSION_ID, 1L);
        verify(cartItemRepository, times(1)).save(cartItem1);
    }

    @Test
    void test_updateQuantity_Minus() {
        when(cartItemRepository.findBySessionIdAndItemId(eq(SESSION_ID), eq(1L)))
                .thenReturn(Mono.just(cartItem1));
        when(cartItemRepository.save(eq(cartItem1)))
                .thenReturn(Mono.just(cartItem1));

        Mono<Void> result = cartService.updateQuantity(SESSION_ID, 1L, "MINUS");

        StepVerifier.create(result)
                .verifyComplete();

        verify(cartItemRepository, times(1))
                .findBySessionIdAndItemId(SESSION_ID, 1L);
        verify(cartItemRepository, times(1)).save(cartItem1);
    }

    @Test
    void test_updateQuantity_Delete() {
        when(cartItemRepository.findBySessionIdAndItemId(eq(SESSION_ID), eq(1L)))
                .thenReturn(Mono.just(cartItem1));
        when(cartItemRepository.delete(eq(cartItem1)))
                .thenReturn(Mono.empty());

        Mono<Void> result = cartService.updateQuantity(SESSION_ID, 1L, "DELETE");

        StepVerifier.create(result)
                .verifyComplete();

        verify(cartItemRepository, times(1))
                .findBySessionIdAndItemId(SESSION_ID, 1L);
        verify(cartItemRepository, times(1)).delete(cartItem1);
    }

    @Test
    void test_updateQuantity_InvalidAction() {
        Mono<Void> result = cartService.updateQuantity(SESSION_ID, 1L, "INVALID");

        StepVerifier.create(result)
                .verifyComplete();

        verify(cartItemRepository, never())
                .findBySessionIdAndItemId(anyString(), anyLong());
        verify(cartItemRepository, never()).save(any());
        verify(cartItemRepository, never()).delete(any());
    }


    @Test
    void test_updateQuantity_NullAction() {
        Mono<Void> result = cartService.updateQuantity(SESSION_ID, 1L, null);

        StepVerifier.create(result)
                .verifyComplete();

        verify(cartItemRepository, never())
                .findBySessionIdAndItemId(anyString(), anyLong());
        verify(cartItemRepository, never()).save(any());
        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void test_getItemCount_ItemExists() {
        when(cartItemRepository.findBySessionIdAndItemId(eq(SESSION_ID), eq(1L)))
                .thenReturn(Mono.just(cartItem1));

        Mono<Integer> result = cartService.getItemCount(SESSION_ID, 1L);

        StepVerifier.create(result)
                .expectNext(2) // количество из cartItem1
                .verifyComplete();

        verify(cartItemRepository, times(1))
                .findBySessionIdAndItemId(SESSION_ID, 1L);
    }

    @Test
    void test_getItemCount_ItemNotInCart() {
        when(cartItemRepository.findBySessionIdAndItemId(eq(SESSION_ID), eq(999L)))
                .thenReturn(Mono.empty());

        Mono<Integer> result = cartService.getItemCount(SESSION_ID, 999L);

        StepVerifier.create(result)
                .expectNext(0)
                .verifyComplete();

        verify(cartItemRepository, times(1))
                .findBySessionIdAndItemId(SESSION_ID, 999L);
    }

    @Test
    void test_removeItem_ItemNotExists() {
        when(cartItemRepository.findBySessionIdAndItemId(eq(SESSION_ID), eq(999L)))
                .thenReturn(Mono.empty());

        Mono<Void> result = cartService.removeItem(SESSION_ID, 999L);

        StepVerifier.create(result)
                .verifyComplete();

        verify(cartItemRepository, times(1))
                .findBySessionIdAndItemId(SESSION_ID, 999L);
        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void test_modifyItem_NegativeQuantityButItemExists() {
        cartItem1.setQuantity(3); // текущее количество — 3
        when(cartItemRepository.findBySessionIdAndItemId(eq(SESSION_ID), eq(1L)))
                .thenReturn(Mono.just(cartItem1));
        when(cartItemRepository.save(eq(cartItem1)))
                .thenReturn(Mono.just(cartItem1));

        Mono<Void> result = cartService.modifyItem(SESSION_ID, 1L, -2);

        StepVerifier.create(result)
                .verifyComplete();

        assertThat(cartItem1.getQuantity()).isEqualTo(1);
        verify(cartItemRepository, times(1))
                .findBySessionIdAndItemId(SESSION_ID, 1L);
        verify(cartItemRepository, times(1)).save(cartItem1);
        verify(cartItemRepository, never()).delete(any());
    }
}