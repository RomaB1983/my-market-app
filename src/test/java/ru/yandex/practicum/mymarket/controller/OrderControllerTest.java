package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import ru.yandex.practicum.mymarket.dto.OrderDto;
import ru.yandex.practicum.mymarket.service.OrderService;


import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    private OrderDto createOrderDto(Long id) {
        OrderDto dto = new OrderDto();
        dto.setId(id);
        dto.setTotalSum(1000L);
        return dto;
    }

    @Test
    void listOrders_shouldReturnOrdersViewAndPopulateModel() throws Exception {
        List<OrderDto> orders = Arrays.asList(
                createOrderDto(1L),
                createOrderDto(2L)
        );
        when(orderService.getAllOrders()).thenReturn(orders);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attribute("orders", hasSize(2)))
                .andExpect(model().attribute("orders", orders));


        verify(orderService).getAllOrders();
    }

    @Test
    void showOrder_shouldReturnOrderView() throws Exception {
        OrderDto order = createOrderDto(1L);
        when(orderService.getOrderById(1L)).thenReturn(order);

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attribute("order", order))
                .andExpect(model().attribute("newOrder", false));  // По умолчанию false

       verify(orderService).getOrderById(1L);
    }

    @Test
    void showOrder_withNewOrderParam_shouldSetNewOrderToTrue() throws Exception {
        OrderDto order = createOrderDto(1L);
        when(orderService.getOrderById(1L)).thenReturn(order);

        mockMvc.perform(get("/orders/1?newOrder=true"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attribute("order", order))
                .andExpect(model().attribute("newOrder", true));

        verify(orderService).getOrderById(1L);
    }

    @Test
    void createOrder_shouldCreateOrderAndRedirect() throws Exception {
        OrderDto savedOrder = createOrderDto(100L);
        when(orderService.createOrder()).thenReturn(savedOrder);

        mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/100?newOrder=true"));


        verify(orderService).createOrder();
    }
}