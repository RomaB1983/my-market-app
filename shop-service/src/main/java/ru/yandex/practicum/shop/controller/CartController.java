package ru.yandex.practicum.shop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.dto.Params;
import ru.yandex.practicum.shop.service.CartService;
import ru.yandex.practicum.shop.service.PaymentService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final PaymentService paymentService;

    @GetMapping("/items")
    public Mono<Rendering> getCartItems
            (WebSession session,
             @RequestParam(required = false) String paymentError) {
        return cartService.getCartItems(session.getId())
                .flatMap(items -> {
                            long total = items.stream()
                                    .mapToLong(item -> item.getPrice() * item.getCount())
                                    .sum();
                            return paymentService.getUserBalance(session.getId())
                                    .map(balance ->
                                            Rendering.view("cart")
                                                    .modelAttribute("items", items)
                                                    .modelAttribute("total", total)
                                                    .modelAttribute("isOkBalance", balance>=total)
                                                    .modelAttribute("saldo", balance)
                                                    .modelAttribute("paymentError", paymentError)
                                                    .build());
                        }
                );
    }

    @PostMapping("/items")
    public Mono<String> updateCartItem(WebSession session, @ModelAttribute Params params) {
        return cartService.updateQuantity(session.getId(), params.getId(), params.getAction())
                .thenReturn("redirect:/cart/items");
    }
}
