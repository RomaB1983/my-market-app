package ru.yandex.practicum.shop.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
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
             @RegisteredOAuth2AuthorizedClient("shop-client") OAuth2AuthorizedClient authorizedClient,
             @RequestParam(required = false) String paymentError) {
        if (authorizedClient == null) {
            log.warn("🚫 No authorized client found for shop-client");
        } else {
            OAuth2AccessToken token = authorizedClient.getAccessToken();
            log.info("🔑 Using OAuth2 token for payment service call:");
            log.info("  Token: {}", token.getTokenValue());
            log.info("  Expires: {}", token.getExpiresAt());
            log.info("  Scopes: {}", token.getScopes());
        }
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
