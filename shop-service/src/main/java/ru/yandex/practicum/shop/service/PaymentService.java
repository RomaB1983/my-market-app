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

    public Mono<Long> getUserBalance(String userId) {
        return paymentApi.getBalance(userId)
                .map(BalanceResponse::getSaldo)
                .doOnNext(saldo -> log.debug("Баланс: {}, userId: {}", saldo, userId))
                .onErrorResume(e -> {
                    log.error("Не удалось получить баланс по userId: {}: {}", userId, e.getMessage());
                    return Mono.just(0L);
                });
    }

    public Mono<PaymentStatus> createPayment(String userId, Long amount) {
        PaymentRequest request = new PaymentRequest().totalSum(amount);
        return paymentApi.createPayment(userId, request)
                .map(PaymentResponse::getStatus)
                .doOnNext(status ->
                        log.debug("CreatePayment: userId: {} totalSum: {} status: {}",
                                userId, amount, status))
                .onErrorResume(e -> {
                    log.error("Не удалось выполнить оплату userId: {} totalSum: {} :{}", userId, amount, e.getMessage());
                    return Mono.just(PaymentStatus.ERROR);
                });
    }
}
