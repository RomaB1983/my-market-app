package ru.yandex.practicum.mymarket.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class Params {
    private Long id;
    private String sort = "NO";
    private int pageNumber = 1;
    private int pageSize = 5;
    private String action;
    private String search;
}
