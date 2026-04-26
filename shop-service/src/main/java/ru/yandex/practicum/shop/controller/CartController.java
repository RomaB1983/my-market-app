package ru.yandex.practicum.shop.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.dto.Params;
import ru.yandex.practicum.shop.service.CartService;
import ru.yandex.practicum.shop.service.PaymentService;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final PaymentService paymentService;

    @GetMapping("/items")
    public Mono<Rendering> getCartItems
            (@AuthenticationPrincipal UserDetails user,
             @RequestParam(required = false) String paymentError) {
        return cartService.getCartItems(user.getUsername())
                .flatMap(items -> {
                            long total = items.stream()
                                    .mapToLong(item -> item.getPrice() * item.getCount())
                                    .sum();
                            return paymentService.getUserBalance(user.getUsername())
                                    .map(balance ->
                                            Rendering.view("cart")
                                                    .modelAttribute("items", items)
                                                    .modelAttribute("total", total)
                                                    .modelAttribute("isOkBalance", balance >= total)
                                                    .modelAttribute("saldo", balance)
                                                    .modelAttribute("paymentError", paymentError)
                                                    .build());
                        }
                );
    }

    @PostMapping("/items")
    public Mono<String> updateCartItem(@AuthenticationPrincipal UserDetails user, @ModelAttribute Params params) {
        return cartService.updateQuantity(user.getUsername(), params.getId(), params.getAction())
                .thenReturn("redirect:/cart/items");
    }
}
