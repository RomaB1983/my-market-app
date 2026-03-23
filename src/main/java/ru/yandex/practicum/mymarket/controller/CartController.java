package ru.yandex.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.Params;
import ru.yandex.practicum.mymarket.service.CartService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    @GetMapping("/items")
    public Mono<Rendering> getCartItems(WebSession session) {
        return cartService.getCartItems(session.getId())
                .map(items -> {
                            long total = items.stream()
                                    .mapToLong(item -> item.getPrice() * item.getCount())
                                    .sum();
                            return Rendering.view("cart")
                                    .modelAttribute("items", items)
                                    .modelAttribute("total", total)
                                    .build();
                        }
                );
    }

    @PostMapping("/items")
    public Mono<String> updateCartItem(WebSession session, @ModelAttribute Params params) {
        return cartService.updateQuantity(session.getId(), params.getId(), params.getAction())
                .thenReturn("redirect:/cart/items");
    }
}
