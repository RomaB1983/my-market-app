package ru.yandex.practicum.shop.config;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class SessionFilter implements WebFilter {

    @Override
    @NonNull
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        return exchange.getSession()
                .flatMap(session -> {
                    if (!session.isStarted()) {
                        log.info("Стартуем WebSession id:{}",session.getId() );
                        session.start();
                    }
                    return chain.filter(exchange);
                });
    }
}