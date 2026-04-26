package ru.yandex.practicum.payment.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
@Slf4j
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) throws Exception {
        http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/balance").hasAuthority("SCOPE_payments.read")
                        .pathMatchers("/api/pay").hasAuthority("SCOPE_payments.write")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {})
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .addFilterAt((exchange, chain) -> {
                    ServerHttpRequest request = exchange.getRequest();
                    HttpHeaders headers = request.getHeaders();

                    log.info("=== REQUEST HEADERS ===");
                    log.info("Method: {}", request.getMethod());
                    log.info("Path: {}", request.getPath().value());

                    headers.forEach((name, values) ->
                            log.info("Header '{}': {}", name, values));

                    return chain.filter(exchange);
                }, SecurityWebFiltersOrder.FIRST);
        ;
        return http.build();
    }

}