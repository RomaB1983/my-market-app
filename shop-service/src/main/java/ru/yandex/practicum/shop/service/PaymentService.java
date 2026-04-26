package ru.yandex.practicum.shop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.client.api.PaymentApi;
import ru.yandex.practicum.shop.client.model.BalanceResponse;
import ru.yandex.practicum.shop.client.model.PaymentRequest;
import ru.yandex.practicum.shop.client.model.PaymentResponse;
import ru.yandex.practicum.shop.client.model.PaymentStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentApi paymentApi;

    public Mono<Long> getUserBalance(String username) {
        return paymentApi.getBalance(username)
                .map(BalanceResponse::getSaldo)
                .doOnNext(saldo -> log.debug("Баланс: {}, username: {}", saldo, username))
                .onErrorResume(e -> {
                    log.error("Не удалось получить баланс по username: {}: {}", username, e.getMessage());
                    return Mono.just(0L);
                });
    }

    public Mono<PaymentStatus> createPayment(String username, Long amount) {
        PaymentRequest request = new PaymentRequest().totalSum(amount);
        return paymentApi.createPayment(username, request)
                .map(PaymentResponse::getStatus)
                .doOnNext(status ->
                        log.debug("CreatePayment: username: {} totalSum: {} status: {}",
                                username, amount, status))
                .onErrorResume(e -> {
                    log.error("Не удалось выполнить оплату username: {} totalSum: {} :{}", username, amount, e.getMessage());
                    return Mono.just(PaymentStatus.ERROR);
                });
    }
}
