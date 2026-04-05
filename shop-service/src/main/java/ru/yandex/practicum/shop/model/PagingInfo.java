package ru.yandex.practicum.shop.model;

import lombok.Data;

@Data
public class PagingInfo {
    private final int pageSize;
    private final int pageNumber;
    private final boolean hasPrevious;
    private final boolean hasNext;
}