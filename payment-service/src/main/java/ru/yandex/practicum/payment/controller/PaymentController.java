package ru.yandex.practicum.payment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    private final PaymentService paymentService;

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getBalance(String username, ServerWebExchange exchange) {
        log.info("Запрос на получение баланса. username: {}", username);
        return paymentService.getBalance(username)
                .map(saldo -> {
                    BalanceResponse balanceResponse = new BalanceResponse(saldo);
                    return ResponseEntity.ok(balanceResponse);
                })
                .onErrorResume(IllegalArgumentException.class, e ->
                        {
                            log.error(e.getMessage());
                            return Mono.just(ResponseEntity.notFound().build());
                        }
                );
    }

    @Override
    public Mono<ResponseEntity<PaymentResponse>> createPayment(String username, Mono<PaymentRequest> paymentRequest, ServerWebExchange exchange) {
        return paymentRequest.flatMap(
                request -> paymentService.createPayment(username, request.getTotalSum())
                        .map(v -> {
                            PaymentResponse body = new PaymentResponse();
                            body.setStatus(PaymentStatus.SUCCESS);
                            body.setDescription("Успешное выполнение оплаты");
                            return ResponseEntity.ok(body);
                        })
                        .onErrorResume(IllegalArgumentException.class, e ->
                                {
                                    PaymentResponse response = genErrorResponse(e, username);
                                    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(response));
                                }
                        )
                        .onErrorResume(IllegalStateException.class, e -> {
                            PaymentResponse response = genErrorResponse(e, username);
                            return Mono.just(ResponseEntity.badRequest().body(response));
                        })
        );
    }

    private PaymentResponse genErrorResponse(Exception e, String username) {
        log.error("Ошибка оплаты. username:" + username + " " + e.getMessage());
        PaymentResponse body = new PaymentResponse();
        body.setStatus(PaymentStatus.ERROR);
        body.setDescription("Ошибка оплаты: " + e.getMessage());
        return body;
    }
}
