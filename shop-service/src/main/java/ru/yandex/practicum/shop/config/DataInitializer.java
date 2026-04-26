package ru.yandex.practicum.shop.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.model.User;
import ru.yandex.practicum.shop.repository.UserRepository;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        Mono.when(
                initUsers()
        ).subscribe(
                null,
                err -> log.error("DataInitializer: ошибка при инициализации данных", err)
        );
    }

      private Mono<Void> initUsers() {
        return userRepository.count()
                .flatMap(count -> {
                    if (count > 0) {
                        log.info("DataInitializer: таблица users уже содержит {} записей, пропускаем.", count);
                        return Mono.empty();
                    }
                    log.info("DataInitializer: создаём пользователей по умолчанию...");
                    return populateDefaultUsers();
                });
    }

    private Mono<Void> populateDefaultUsers() {
        List<User> users = List.of(
                User.builder()
                        .username("user")
                        .password(passwordEncoder.encode("user"))
                        .role("USER")
                        .build(),
                User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .role("ADMIN")
                        .build()
        );

        return Flux.fromIterable(users)
                .concatMap(userRepository::save)
                .doOnNext(saved -> log.info("DataInitializer: создан пользователь username={}",
                        saved.getUsername()))
                .then()
                .doOnSuccess(v -> log.info("DataInitializer: пользователи по умолчанию созданы."));
    }
}