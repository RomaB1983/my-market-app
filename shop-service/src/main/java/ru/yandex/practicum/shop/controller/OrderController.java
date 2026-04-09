package ru.yandex.practicum.shop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.service.OrderService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/orders")
    public Mono<Rendering> getAllOrders(WebSession session) {
        return orderService.getAllOrders(session.getId())
                .map(orders -> Rendering.view("orders")
                        .modelAttribute("orders", orders)
                        .build());
    }

    @GetMapping("/orders/{id}")
    public Mono<Rendering> getOrder(WebSession session,
                                    @PathVariable Long id,
                                    @RequestParam(required = false) boolean newOrder
    ) {
        return orderService.getOrderById(session.getId(), id)
                .map(order -> Rendering.view("order")
                        .modelAttribute("order", order)
                        .modelAttribute("newOrder", newOrder)
                        .build()
                );
    }

    @PostMapping("/buy")
    public Mono<String> createOrder(WebSession session) {
        return orderService.createOrder(session.getId())
                .flatMap(savedOrder -> Mono.just("redirect:/orders/" +
                        savedOrder.getId() + "?newOrder=true"))
                .onErrorResume(IllegalStateException.class, e ->
                        Mono.just("redirect:/cart/items?paymentError=" +
                                URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8)));
    }
}

