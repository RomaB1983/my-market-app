package ru.yandex.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.mymarket.dto.ItemDto;
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
    public String showItems(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "NO") String sort,
            @RequestParam(required = false, defaultValue = "1") int pageNumber,
            @RequestParam(required = false, defaultValue = "5") int pageSize,
            Model model) {

        List<ItemDto> items = itemService.getAllItems(search, sort, pageNumber, pageSize);
        // Группируем товары по 3 для отображения в сетке
        List<List<ItemDto>> groupedItems = new ArrayList<>();
        for (int i = 0; i < items.size(); i += 3) {
            List<ItemDto> row = items.subList(i, Math.min(i + 3, items.size()));
            groupedItems.add(row);
        }
        model.addAttribute("items", groupedItems);
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        model.addAttribute("paging", new PagingInfo(pageSize, pageNumber, pageNumber > 1,
                items.size() == pageSize && (!itemService.getAllItems(search, sort, pageNumber + 1, pageSize).isEmpty())));


        return "items";
    }

    @GetMapping("/items/{id}")
    public String showItem(@PathVariable Long id, Model model) {
        fillModelForItem(model,id);
        return "item";
    }

    @PostMapping("/items")
    public String updateCart(
            @RequestParam Long id,
            @RequestParam String action,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "NO") String sort,
            @RequestParam(required = false, defaultValue = "1") int pageNumber,
            @RequestParam(required = false, defaultValue = "5") int pageSize,
            Model model) {


        if ("PLUS".equals(action)) {
            cartService.addToCart(id, 1);
        } else if ("MINUS".equals(action)) {
            cartService.updateQuantity(id, cartService.getItemCount(id) - 1);
        }

        return "redirect:/items?search=" + (search != null ? search : "") +
                "&sort=" + sort +
                "&pageNumber=" + pageNumber +
                "&pageSize=" + pageSize;
    }

    @PostMapping("/items/{id}")
    public String updateItemCart(@PathVariable Long id, @RequestParam String action, Model model) {
        if ("PLUS".equals(action)) {
            cartService.addToCart(id, 1);
        } else if ("MINUS".equals(action)) {
            cartService.updateQuantity(id, cartService.getItemCount(id) - 1);
        }
        fillModelForItem(model,id);
        return "item";
    }

    private void fillModelForItem(Model model, Long id){
        ItemDto item = itemService.getItemById(id);
        item.setCount(cartService.getItemCount(id));
        model.addAttribute("item", item);
    }

}

