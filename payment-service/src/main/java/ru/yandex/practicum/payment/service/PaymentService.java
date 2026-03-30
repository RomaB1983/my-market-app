package ru.yandex.practicum.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.model.User;
import ru.yandex.practicum.payment.repository.UserRepository;
import ru.yandex.practicum.payment.util.BalanceGenerator;

import java.math.BigDecimal;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentService {
    UserRepository userRepository;

    @Cacheable(value = "userBalance", key = "#userId")
    public Mono<BigDecimal> getBalance(String userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.defer(() -> createUser(userId)))
                .map(User::getSaldo);
    }

    private Mono<User> createUser(String userId) {
        return userRepository.save(new User(userId, BalanceGenerator.get()));
    }

    @CacheEvict(value = "userBalance", key = "#userId")
    public Mono<User> processPayment(String userId, BigDecimal totalSum) {
        log.info("Запрос на оплату заказа. userId: {}, сумма оплаты: {}", userId, totalSum);
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.defer(() -> createUser(userId)))
                .flatMap(user -> {
                    if (user.getSaldo().compareTo(totalSum) >= 0) {
                        user.setSaldo(user.getSaldo().subtract(totalSum));
                        log.info("Успешная оплата заказа. userId: {}, сумма оплаты: {}, баланс после оплаты: {}"
                                , userId, totalSum, user.getSaldo());
                        return userRepository.save(user);
                    } else {
                        log.warn("Недостаточно средств для оплаты: Баланс: {}, Сумма оплаты: {}", user.getSaldo(), totalSum);
                        return Mono.error(new IllegalStateException("Недостаточно средств для оплаты: " +
                                "Баланс: " + user.getSaldo() + " сумма оплаты: " + totalSum));
                    }
                });
    }
}