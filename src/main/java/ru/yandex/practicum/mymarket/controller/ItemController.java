package ru.yandex.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.Params;
import ru.yandex.practicum.mymarket.model.PagingInfo;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.ItemService;

import java.util.ArrayList;
import java.util.List;


@Controller
@RequiredArgsConstructor
@CrossOrigin
public class ItemController {

    private final ItemService itemService;
    private final CartService cartService;

    @GetMapping({"/", "/items"})
    public Mono<Rendering> getAllItems(
            WebSession session,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "NO") String sort,
            @RequestParam(required = false, defaultValue = "1") int pageNumber,
            @RequestParam(required = false, defaultValue = "5") int pageSize
    ) {
        return itemService.getAllItems(search, sort, pageNumber, pageSize, session.getId())
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
    public Mono<Rendering> getItem(WebSession session, @PathVariable Long id) {
        return itemService.getItemById(session.getId(), id)
                .map(item -> Rendering.view("item")
                        .modelAttribute("item", item)
                        .build());
    }

    @PostMapping("/items")
    public Mono<String> updateCart(WebSession session, @ModelAttribute Params params) {
        return cartService.updateQuantity(session.getId(), params.getId(), params.getAction())
                .thenReturn("redirect:/items?search=" + (params.getSearch() != null ? params.getSearch() : "") +
                        "&sort=" + params.getSort() +
                        "&pageNumber=" + params.getPageNumber() +
                        "&pageSize=" + params.getPageSize());

    }

    @PostMapping("/items/{id}")
    public Mono<String> updateCartItem(WebSession session, @PathVariable Long id, @ModelAttribute Params params) {
        return cartService.updateQuantity(session.getId(), params.getId(), params.getAction())
                .thenReturn("redirect:/items/" + id);
    }
}