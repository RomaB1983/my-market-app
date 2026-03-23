package ru.yandex.practicum.mymarket.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ORDER_ITEMS")
public class OrderItem {
    @Id
    private Long id;
    private Long itemId;
    private Long orderId;
    private Integer count;
    private Long price;
}