package ru.yandex.practicum.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
//    @JsonIgnore
    private Integer count;
}
