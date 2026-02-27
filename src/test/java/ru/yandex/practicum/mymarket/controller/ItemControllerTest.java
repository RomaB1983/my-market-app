package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.ItemService;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private CartService cartService;

    // Вспомогательный метод для создания ItemDto
    private ItemDto createItemDto(Long id, String name) {
        ItemDto dto = new ItemDto();
        dto.setId(id);
        dto.setTitle(name);
        dto.setPrice(100L);
        return dto;
    }

    @Test
    void showItems_shouldReturnItemsViewAndPopulateModel() throws Exception {
        List<ItemDto> items = Arrays.asList(
                createItemDto(1L, "Товар 1"),
                createItemDto(2L, "Товар 2"),
                createItemDto(3L, "Товар 3")
        );
        when(itemService.getAllItems("", "NO", 1, 5)).thenReturn(items);

        mockMvc.perform(get("/items")
                        .param("search","")
                        .param("sort","NO")
                        .param("pageNumber","1")
                        .param("pageSize","5"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"))
                .andExpect(model().attribute("search", ""))
                .andExpect(model().attribute("items", hasSize(1)))
                .andExpect(model().attribute("sort", "NO"))
                .andExpect(model().attributeExists("paging"));

        verify(itemService).getAllItems("", "NO", 1, 5);
    }

    @Test
    void showItem_shouldReturnItemView() throws Exception {
        ItemDto item = createItemDto(1L, "Товар 1");
        when(itemService.getItemById(1L)).thenReturn(item);
        when(cartService.getItemCount(1L)).thenReturn(2);

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attribute("item", item));

        verify(itemService).getItemById(1L);
        verify(cartService).getItemCount(1L);
    }

    @Test
    void updateCart_plusAction_shouldAddToCartAndRedirect() throws Exception {
        mockMvc.perform(post("/items")
                        .param("id", "1")
                        .param("action", "PLUS")
                        .param("search", "test")
                        .param("sort", "PRICE")
                        .param("pageNumber", "2")
                        .param("pageSize", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items?search=test&sort=PRICE&pageNumber=2&pageSize=10"));

        verify(cartService).addToCart(1L, 1);
    }

    @Test
    void updateCart_minusAction_shouldUpdateQuantityAndRedirect() throws Exception {
        when(cartService.getItemCount(1L)).thenReturn(3);

        mockMvc.perform(post("/items")
                        .param("id", "1")
                        .param("action", "MINUS")
                        .param("search", "")
                        .param("sort", "NO")
                        .param("pageNumber", "1")
                        .param("pageSize", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items?search=&sort=NO&pageNumber=1&pageSize=5"));


        verify(cartService).updateQuantity(1L, 2);
    }

    @Test
    void updateItemCart_plusAction_shouldAddToCartAndReturnItemView() throws Exception {
        ItemDto item = createItemDto(1L, "Товар 1");
        when(itemService.getItemById(1L)).thenReturn(item);
        when(cartService.getItemCount(1L)).thenReturn(1);

        mockMvc.perform(post("/items/1")
                        .param("action", "PLUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attribute("item", item));

        verify(cartService).addToCart(1L, 1);
    }

    @Test
    void updateItemCart_minusAction_shouldDecreaseQuantityAndReturnItemView() throws Exception {
        ItemDto item = createItemDto(1L, "Товар 1");
        when(itemService.getItemById(1L)).thenReturn(item);
        when(cartService.getItemCount(1L)).thenReturn(2);

        mockMvc.perform(post("/items/1")
                        .param("action", "MINUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attribute("item", item));

        verify(cartService).updateQuantity(1L, 1);
    }

    @Test
    void showItems_withSearchAndSort_shouldPassToService() throws Exception {
        List<ItemDto> items = Arrays.asList(createItemDto(1L, "Найденный товар"));
        when(itemService.getAllItems("test", "PRICE", 1, 5)).thenReturn(items);

        mockMvc.perform(get("/items")
                        .param("search", "test")
                        .param("sort", "PRICE"))
                .andExpect(status().isOk());

        verify(itemService).getAllItems("test", "PRICE", 1, 5);
    }
}
