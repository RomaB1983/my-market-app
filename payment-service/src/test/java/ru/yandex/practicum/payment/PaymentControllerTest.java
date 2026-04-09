package ru.yandex.practicum.payment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.model.*;
import ru.yandex.practicum.payment.repository.UserRepository;
import ru.yandex.practicum.payment.service.PaymentService;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@SpringBootTest
@AutoConfigureWebTestClient
class PaymentControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private UserRepository userRepository;


    @Test
    void test_getBalance_Success() {
        // Given
        String userId = "user123";
        Long expectedBalance = 1000L;

        when(paymentService.getBalance(anyString()))
                .thenReturn(Mono.just(expectedBalance));
        when(userRepository.findById(anyString()))
                .thenReturn(Mono.just(new User(userId, expectedBalance + 100)));

        // When & Then
        webTestClient.get()
                .uri("/api/balance")
                .header("UserId", userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(BalanceResponse.class)
                .value(response -> {
                    assert response.getSaldo().equals(expectedBalance);
                });
    }

    @Test
    void test_getBalance_NotFound() {
        // Given
        String userId = "nonexistent";

        when(paymentService.getBalance(anyString()))
                .thenReturn(Mono.error(new IllegalArgumentException("User not found")));

        // When & Then
        webTestClient.get()
                .uri("api/balance")
                .header("UserId",userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void test_createPayment_Success() {
        String userId = "user123";
        Long amount = 500L;
        User user = new User(userId, amount + 100);
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setTotalSum(amount);

        when(paymentService.createPayment(anyString(), anyLong()))
                .thenReturn(Mono.just(user));

        when(userRepository.findById(userId))
                .thenReturn(Mono.just(user));

        webTestClient.post()
                .uri("/api/pay")
                .header("UserId", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(paymentRequest))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(PaymentResponse.class)
                .value(response -> {
                    assert PaymentStatus.SUCCESS.equals(response.getStatus());
                    assert "Успешное выполнение оплаты".equals(response.getDescription());
                });
    }
}
