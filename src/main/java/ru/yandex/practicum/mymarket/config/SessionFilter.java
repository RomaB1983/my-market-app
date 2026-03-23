package ru.yandex.practicum.mymarket.config;

import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class SessionFilter implements WebFilter {

    @Override
    @NonNull
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        return exchange.getSession()
                .flatMap(session -> {
                    if (!session.isStarted()) {
                        session.start();
                    }
                    return chain.filter(exchange);
                });
    }
}