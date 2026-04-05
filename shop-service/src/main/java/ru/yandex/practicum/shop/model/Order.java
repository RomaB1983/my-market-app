package ru.yandex.practicum.shop.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ORDERS")
public class Order {
    @Id
    private Long id;
    @Transient
    private List<OrderItem> items;
    private Long totalSum;
    private String sessionId;
}