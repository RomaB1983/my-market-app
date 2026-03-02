package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.service.CartService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @Test
    void showCart_shouldReturnCartViewAndPopulateModel() throws Exception {
        when(cartService.getCartItems()).thenReturn(List.of(
                new ItemDto(1L, "Товар 1", "Товар 1", "_", 2L, 2)
        ));
        when(cartService.getTotalPrice()).thenReturn(4L);

        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("items", hasSize(1)))
                .andExpect(model().attribute("total", 4L));

        verify(cartService).getCartItems();
        verify(cartService).getTotalPrice();
    }

    @Test
    void updateItemCart_plusAction_shouldAddOneToQuantity() throws Exception {
        mockMvc.perform(post("/cart/items")
                        .param("id", "1")
                        .param("action", "PLUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));

        verify(cartService).addToCart(1L, 1);
    }

    @Test
    void updateItemCart_minusAction_shouldDecreaseQuantity() throws Exception {
        when(cartService.getItemCount(1L)).thenReturn(3);

        mockMvc.perform(post("/cart/items")
                        .param("id", "1")
                        .param("action", "MINUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));

        verify(cartService).updateQuantity(1L, 2);
    }

    @Test
    void updateItemCart_deleteAction_shouldSetQuantityToZero() throws Exception {
        when(cartService.getItemCount(1L)).thenReturn(3);

        mockMvc.perform(post("/cart/items")
                        .param("id", "1")
                        .param("action", "DELETE"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));

        verify(cartService).updateQuantity(1L, 0);
    }

    @Test
    void updateItemCart_invalidAction_shouldDoNothing() throws Exception {
        mockMvc.perform(post("/cart/items")
                        .param("id", "1")
                        .param("action", "UNKNOWN"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));

        verify(cartService, never()).updateQuantity(anyLong(), anyInt());
    }
}