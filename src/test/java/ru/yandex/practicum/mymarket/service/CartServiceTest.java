package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = CartService.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class CartServiceTest {

    @MockitoBean
    private ItemRepository itemRepository;

    @Autowired
    private CartService cartService;

    private Item item;

    @BeforeEach
    void setUp() {

        item = new Item();
        item.setId(1L);
        item.setTitle("Тест‑товар");
        item.setDescription("Описание");
        item.setImgPath("/image/1.jpg");
        item.setPrice(100L);


        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setTitle("Тест‑товар");
        itemDto.setPrice(100L);
        cartService.clearCart();
    }

    @Test
    void addToCart_shouldAddNewItem() {
        cartService.addToCart(1L, 2);
        assertThat(cartService.getItemCount(1L)).isEqualTo(2);
    }

    @Test
    void addToCart_shouldAccumulateQuantity() {
        cartService.addToCart(1L, 2);
        cartService.addToCart(1L, 3);

        assertThat(cartService.getItemCount(1L)).isEqualTo(5);
    }

    @Test
    void removeFromCart_shouldRemoveItem() {
        cartService.addToCart(1L, 5);
        cartService.removeFromCart(1L);

        assertThat(cartService.getItemCount(1L)).isZero();
    }

    @Test
    void updateQuantity_shouldSetNewQuantity() {
        cartService.addToCart(1L, 2);
        cartService.updateQuantity(1L, 7);

        assertThat(cartService.getItemCount(1L)).isEqualTo(7);
    }

    @Test
    void updateQuantity_withZeroOrNegative_shouldRemoveItem() {
        cartService.addToCart(1L, 5);

        cartService.updateQuantity(1L, 0);
        assertThat(cartService.getItemCount(1L)).isZero();

        cartService.addToCart(1L, 3);
        cartService.updateQuantity(1L, -1);
        assertThat(cartService.getItemCount(1L)).isZero();
    }

    @Test
    void getCartItems_shouldReturnDtoWithCount() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        cartService.addToCart(1L, 4);

        List<ItemDto> cartItems = cartService.getCartItems();

        assertThat(cartItems).hasSize(1);

        ItemDto result = cartItems.getFirst();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCount()).isEqualTo(4);
        assertThat(result.getPrice()).isEqualTo(100L);
    }

    @Test
    void getCartItems_whenItemNotFound_shouldThrowException() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        cartService.addToCart(999L, 1);

        assertThatThrownBy(() -> cartService.getCartItems())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Item not found");
    }

    @Test
    void getCartItems_shouldHandleEmptyCart() {
        List<ItemDto> cartItems = cartService.getCartItems();
        assertThat(cartItems).isEmpty();
    }

    @Test
    void getTotalPrice_shouldCalculateCorrectly() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item));

        cartService.addToCart(1L, 2);  // 2 × 100 = 200
        cartService.addToCart(2L, 3);  // 3 × 100 = 300

        long total = cartService.getTotalPrice();
        assertThat(total).isEqualTo(500L);
    }

    @Test
    void getTotalPrice_whenCartEmpty_shouldReturnZero() {
        long total = cartService.getTotalPrice();
        assertThat(total).isZero();
    }

    @Test
    void getItemCount_shouldReturnZeroForMissingItem() {
        assertThat(cartService.getItemCount(999L)).isZero();
    }
}
