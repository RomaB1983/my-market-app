package ru.yandex.practicum.payment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.model.BalanceResponse;
import ru.yandex.practicum.payment.model.PaymentRequest;
import ru.yandex.practicum.payment.model.PaymentResponse;
import ru.yandex.practicum.payment.model.PaymentStatus;
import ru.yandex.practicum.payment.service.PaymentService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {
    PaymentService paymentService;

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getBalance(String userId, ServerWebExchange exchange) {
        return paymentService.getBalance(userId)
                .map(saldo -> {
                    BalanceResponse balanceResponse = new BalanceResponse(saldo);
                    return ResponseEntity.ok(balanceResponse);
                })
                .onErrorResume(IllegalArgumentException.class, e ->
                        Mono.just(ResponseEntity.notFound().build())
                );
    }

    @Override
    public Mono<ResponseEntity<PaymentResponse>> processPayment(String userId, Mono<PaymentRequest> paymentRequest, ServerWebExchange exchange) {
        return paymentRequest.flatMap(
                request -> paymentService.processPayment(userId, request.getTotalSum())
                        .map(v -> {
                            PaymentResponse body = new PaymentResponse();
                            body.setStatus(PaymentStatus.SUCCESS);
                            body.setDescription("Успешное выполнение оплаты");
                            return ResponseEntity.ok(body);
                        })
                        .onErrorResume(IllegalArgumentException.class, e ->
                                Mono.just(ResponseEntity.notFound().build())
                        )
                        .onErrorResume(IllegalStateException.class, e -> {
                            PaymentResponse body = new PaymentResponse();
                            body.setStatus(PaymentStatus.ERROR);
                            body.setDescription("Ошибка оплаты: " + e.getMessage());
                            log.error("Ошибка оплаты. userId:" + userId + " " + e.getMessage());
                            return Mono.just(ResponseEntity.badRequest().body(body));
                        })
        );

    }
}
