package ru.yandex.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.mymarket.service.CartService;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/cart/items")
    public String showCart(Model model) {
        fillModelForCart(model);
        return "cart";
    }

    @PostMapping("/cart/items")
    public String updateItemCart(@RequestParam Long id, @RequestParam String action, Model model) {
        switch (action) {
            case "PLUS" -> cartService.addToCart(id, 1);
            case "MINUS" -> cartService.updateQuantity(id, cartService.getItemCount(id) - 1);
            case "DELETE" -> cartService.updateQuantity(id,0);
            default -> {}
        }

        fillModelForCart(model);
        return "cart";
    }

    private void fillModelForCart(Model model){
        model.addAttribute("items", cartService.getCartItems());
        model.addAttribute("total", cartService.getTotalPrice());
    }

}
