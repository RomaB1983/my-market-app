package ru.yandex.practicum.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.model.User;
import ru.yandex.practicum.payment.repository.UserRepository;
import ru.yandex.practicum.payment.util.BalanceGenerator;

import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentService {
    private final UserRepository userRepository;
    private final ReactiveRedisTemplate<String, Long> redisTemplate;
    private static final String CACHE_PREFIX = "user:";
    private static final Duration TTL = Duration.ofMinutes(4);

    public Mono<Long> getBalance(String username) {
        String keyCache = CACHE_PREFIX + username;
        return redisTemplate.opsForValue().get(keyCache)
                .doOnSuccess(saldo ->
                        log.info("Баланс есть в кеше для пользователя username:{}, saldo:{}", username, saldo))
                .switchIfEmpty(Mono.defer(() ->
                        userRepository.findByUsername(username)
                                .switchIfEmpty(Mono.defer(() -> createUser(username)))
                                .flatMap(user -> redisTemplate.opsForValue()
                                        .set(keyCache, user.getSaldo(), TTL)
                                        .doOnSuccess(ok ->
                                                log.info("Баланс положили в кеш для пользователя username:{}, saldo:{}", username, user.getSaldo()))
                                        .thenReturn(user.getSaldo()))

                ));
    }

    private Mono<User> createUser(String username) {
        log.info("Создаем нового пользователя. username: {}", username);
        Long balance = BalanceGenerator.get();
        return userRepository.insert(username, balance)
                .then(Mono.just(new User(username, balance)));
    }

    public Mono<User> createPayment(String username, Long totalSum) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.defer(() -> createUser(username)))
                .flatMap(user -> {
                    if (user.getSaldo() > totalSum) {
                        user.setSaldo(user.getSaldo() - totalSum);
                        log.info("Успешная оплата заказа. username: {}, сумма оплаты: {}, баланс после оплаты: {}"
                                , username, totalSum, user.getSaldo());
                        return userRepository.save(user)
                                .flatMap(savedUser -> redisTemplate.opsForValue()
                                        .delete(CACHE_PREFIX + username)
                                        .thenReturn(savedUser));
                    } else {
                        log.warn("Недостаточно средств для оплаты: Баланс: {}, Сумма оплаты: {}", user.getSaldo(), totalSum);
                        return Mono.error(new IllegalStateException("Недостаточно средств для оплаты: " +
                                "Баланс: " + user.getSaldo() + " сумма оплаты: " + totalSum));
                    }
                });
    }
}