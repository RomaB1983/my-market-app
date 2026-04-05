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

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentService {
    private final UserRepository userRepository;

    @Cacheable(value = "user", key = "#userId")
    public Mono<Long> getBalance(String userId) {
        log.info("Запрос на получение баланса из БД. userId: {}", userId);
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.defer(() -> createUser(userId)))
                .map(User::getSaldo);
    }

    private Mono<User> createUser(String userId) {
        log.info("Создаем нового пользователя. userId: {}", userId);
        Long balance = BalanceGenerator.get();
        return userRepository.insert(userId, balance)
                .then(Mono.just(new User(userId,balance)));
    }

    @CacheEvict(value = "user", key = "#userId")
    public Mono<User> createPayment(String userId, Long totalSum) {
        log.info("Запрос на оплату заказа. userId: {}, сумма оплаты: {}", userId, totalSum);
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.defer(() -> createUser(userId)))
                .flatMap(user -> {
                    if (user.getSaldo() > totalSum) {
                        user.setSaldo(user.getSaldo() - totalSum);
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