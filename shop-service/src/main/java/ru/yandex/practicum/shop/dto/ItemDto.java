package ru.yandex.practicum.shop.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;
    private String title;
    private String description;
    private String imgPath;
    private Long price;
    private Integer count;
}
