package ru.yandex.practicum.shop.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.dto.ItemDto;
import ru.yandex.practicum.shop.dto.Params;
import ru.yandex.practicum.shop.model.PagingInfo;
import ru.yandex.practicum.shop.service.CartService;
import ru.yandex.practicum.shop.service.ItemService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@CrossOrigin
public class ItemController {

    private final ItemService itemService;
    private final CartService cartService;

    @GetMapping({"/", "/items"})
    public Mono<Rendering> getAllItems(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "NO") String sort,
            @RequestParam(required = false, defaultValue = "1") int pageNumber,
            @RequestParam(required = false, defaultValue = "5") int pageSize
    ) {
        return
                itemService.getAllItems(search, sort, pageNumber, pageSize, user == null ? "anonymous" : user.getUsername())
                        .map(itemDtoPage -> {
                            List<ItemDto> items = itemDtoPage.getContent();

                            // Группируем товары по 3 для отображения в сетке
                            List<List<ItemDto>> groupedItems = new ArrayList<>();
                            for (int i = 0; i < items.size(); i += 3) {
                                List<ItemDto> row = items.subList(i, Math.min(i + 3, items.size()));
                                groupedItems.add(row);
                            }
                            return Rendering.view("items")
                                    .modelAttribute("items", groupedItems)
                                    .modelAttribute("search", search)
                                    .modelAttribute("sort", sort)
                                    .modelAttribute("paging", new PagingInfo(
                                            pageSize,
                                            pageNumber,
                                            itemDtoPage.hasPrevious(),
                                            itemDtoPage.hasNext())
                                    )
                                    .build();
                        });
    }

    @GetMapping("/items/{id}")
    public Mono<Rendering> getItem(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        log.info("Получаем продукт из кеша id::{}", id);
        return itemService.getItemById(user == null ? "anonymous" : user.getUsername(), id)
                .map(item -> Rendering.view("item")
                        .modelAttribute("item", item)
                        .build());
    }

    @PostMapping("/items")
    public Mono<String> updateCart(@AuthenticationPrincipal UserDetails user, @ModelAttribute Params params) {
        return cartService.updateQuantity(user.getUsername(), params.getId(), params.getAction())
                .thenReturn("redirect:/items?search=" + (params.getSearch() != null ? params.getSearch() : "") +
                        "&sort=" + params.getSort() +
                        "&pageNumber=" + params.getPageNumber() +
                        "&pageSize=" + params.getPageSize());

    }

    @PostMapping("/items/{id}")
    public Mono<String> updateCartItem(@AuthenticationPrincipal UserDetails user,
                                       @PathVariable Long id,
                                       @ModelAttribute Params params) {
        return cartService.updateQuantity(user.getUsername(), params.getId(), params.getAction())
                .thenReturn("redirect:/items/" + id);
    }
}