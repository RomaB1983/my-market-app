package ru.yandex.practicum.shop.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "CART_ITEMS")

public class CartItem {
    @Id
    private Long id;
    private String sessionId;
    private Integer quantity;
    private Long itemId;
}