package ru.yandex.practicum.shop.config;


import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.client.api.PaymentApi;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestCommonConfig {

    @Bean
    @Primary
    public PaymentApi paymentApiMock() {
        return mock(PaymentApi.class);
    }
}