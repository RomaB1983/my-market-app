package ru.yandex.practicum.shop.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.shop.client.api.PaymentApi;
import ru.yandex.practicum.shop.client.model.BalanceResponse;
import ru.yandex.practicum.shop.client.model.PaymentRequest;
import ru.yandex.practicum.shop.client.model.PaymentResponse;
import ru.yandex.practicum.shop.client.model.PaymentStatus;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;
    @MockitoBean
    private PaymentApi paymentApi;
@Autowired
private ReactiveClientRegistrationRepository clientRegistrationRepository;

    @Test
    void test_getUserBalance_Success() {
        String userId = "1";
        Long expectedBalance = 1000L;

        when(paymentApi.getBalance(anyString()))
                .thenReturn(Mono.just(new BalanceResponse().saldo(expectedBalance)));

        Mono<Long> result = paymentService.getUserBalance(userId);

        StepVerifier.create(result)
                .expectNext(expectedBalance)
                .verifyComplete();
    }

    @Test
    void test_getUserBalance_ApiError_ReturnsZero() {
        String userId = "1";

        when(paymentApi.getBalance(anyString()))
                .thenReturn(Mono.error(new RuntimeException("API error")));

        Mono<Long> result = paymentService.getUserBalance(userId);

        StepVerifier.create(result)
                .expectNext(0L)
                .verifyComplete();
    }

    @Test
    void test_createPayment_Success() {
        String userId = "1";
        Long amount = 500L;
        PaymentStatus expectedStatus = PaymentStatus.SUCCESS;
        when(paymentApi.createPayment(anyString(), any(PaymentRequest.class)))
                .thenReturn(Mono.just(new PaymentResponse().status(expectedStatus)));

        Mono<PaymentStatus> result = paymentService.createPayment(userId, amount);
        StepVerifier.create(result)
                .expectNext(expectedStatus)
                .verifyComplete();
    }

    @Test
    void test_createPayment_ApiError_ReturnsErrorStatus() {
        String userId = "1";
        Long amount = 500L;
        when(paymentApi.createPayment(anyString(), any(PaymentRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("Payment API error")));

        Mono<PaymentStatus> result = paymentService.createPayment(userId, amount);
        StepVerifier.create(result)
                .expectNext(PaymentStatus.ERROR)
                .verifyComplete();
    }
}
