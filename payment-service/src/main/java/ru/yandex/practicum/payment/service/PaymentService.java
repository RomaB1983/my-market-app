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

    public Mono<Long> getBalance(String userId) {
        String keyCache = CACHE_PREFIX + userId;
        return redisTemplate.opsForValue().get(keyCache)
                .doOnSuccess(saldo ->
                        log.info("Баланс есть в кеше для пользователя userId:{}, saldo:{}", userId, saldo))
                .switchIfEmpty(Mono.defer(() ->
                        userRepository.findById(userId)
                                .switchIfEmpty(Mono.defer(() -> createUser(userId)))
                                .flatMap(user -> redisTemplate.opsForValue()
                                        .set(keyCache, user.getSaldo(), TTL)
                                        .doOnSuccess(ok ->
                                                log.info("Баланс положили в кеш для пользователя userId:{}, saldo:{}", userId, user.getSaldo()))
                                        .thenReturn(user.getSaldo()))

                ));
    }

    private Mono<User> createUser(String userId) {
        log.info("Создаем нового пользователя. userId: {}", userId);
        Long balance = BalanceGenerator.get();
        return userRepository.insert(userId, balance)
                .then(Mono.just(new User(userId, balance)));
    }

    public Mono<User> createPayment(String userId, Long totalSum) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.defer(() -> createUser(userId)))
                .flatMap(user -> {
                    if (user.getSaldo() > totalSum) {
                        user.setSaldo(user.getSaldo() - totalSum);
                        log.info("Успешная оплата заказа. userId: {}, сумма оплаты: {}, баланс после оплаты: {}"
                                , userId, totalSum, user.getSaldo());
                        return userRepository.save(user)
                                .flatMap(savedUser -> redisTemplate.opsForValue()
                                        .delete(CACHE_PREFIX + userId)
                                        .thenReturn(savedUser));
                    } else {
                        log.warn("Недостаточно средств для оплаты: Баланс: {}, Сумма оплаты: {}", user.getSaldo(), totalSum);
                        return Mono.error(new IllegalStateException("Недостаточно средств для оплаты: " +
                                "Баланс: " + user.getSaldo() + " сумма оплаты: " + totalSum));
                    }
                });
    }
}