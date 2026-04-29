package ru.yandex.practicum.shop.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.practicum.shop.client.ApiClient;
import ru.yandex.practicum.shop.client.api.PaymentApi;

@Slf4j
@Configuration
public class PaymentConfig {

    @Value("${app.payment.service.url:http://localhost:8081}")
    private String paymentServiceUrl;

    @Bean
    public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(
            ReactiveClientRegistrationRepository clientRegistrationRepository,
            ReactiveOAuth2AuthorizedClientService authorizedClientService) {
        var provider = ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();
        var manager = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
                clientRegistrationRepository, authorizedClientService);
        manager.setAuthorizedClientProvider(provider);
        return manager;
    }


    @Bean
    public WebClient paymentWebClient(ReactiveOAuth2AuthorizedClientManager manager) {
        log.info("✅ Creating paymentWebClient bean with base URL: {}", paymentServiceUrl);

        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2Filter =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(manager);
        oauth2Filter.setDefaultClientRegistrationId("shop-client");

        return WebClient.builder()
                .baseUrl(paymentServiceUrl)
                .filter((request, next) -> {
                    // Логи до OAuth2-фильтра
                    log.debug("🔎 BEFORE OAuth2 filter: {} {}", request.method(), request.url());
                    request.headers().forEach((name, values) ->
                            log.debug("Header (before): {} = {}", name, values));
                    return next.exchange(request);
                })
                .filter(oauth2Filter)
                .filter((request, next) -> {
                    // Логи после OAuth2-фильтра — здесь уже должен быть Authorization
                    log.info("📤 AFTER OAuth2 filter - Outgoing request: {} {}", request.method(), request.url());
                    request.headers().forEach((name, values) ->
                            log.info("Header (after): {} = {}", name, values));
                    return next.exchange(request);
                })
                .build();
    }

    @Bean
    public ApiClient apiClient(WebClient paymentWebClient) {
        ApiClient client = new ApiClient(paymentWebClient);
        client.setBasePath(paymentServiceUrl);
        return client;
    }

    @Bean
    public PaymentApi paymentApi(ApiClient apiClient) {
        return new PaymentApi(apiClient);
    }
}

