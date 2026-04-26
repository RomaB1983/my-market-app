package ru.yandex.practicum.shop.dto;

import lombok.Data;

@Data
public class Params {
    private Long id;
    private String sort = "NO";
    private int pageNumber = 1;
    private int pageSize = 5;
    private String action;
    private String search;
}
