package ru.yandex.practicum.shop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.practicum.shop.client.ApiClient;
import ru.yandex.practicum.shop.client.api.PaymentApi;

@Configuration
public class PaymentConfig {

    @Value("${app.payment.service.url:http://localhost:8081}")
    private String paymentServiceUrl;

    @Bean
    public ApiClient apiClient() {
        ApiClient client = new ApiClient(
                WebClient.builder().baseUrl(paymentServiceUrl).build()
        );
        client.setBasePath(paymentServiceUrl);
        return client;
    }

    @Bean
    public PaymentApi paymentApi(ApiClient apiClient) {
        return new PaymentApi(apiClient);
    }
}
