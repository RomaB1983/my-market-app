package ru.yandex.practicum.shop.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class LoginController {
    @GetMapping("/login")
    public String login(@RequestParam(required = false) String logout) {
        return "login";
    }
}