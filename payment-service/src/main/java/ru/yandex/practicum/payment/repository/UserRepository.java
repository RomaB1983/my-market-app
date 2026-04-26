package ru.yandex.practicum.payment.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.model.User;

public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    @Query(value = "INSERT INTO users(username, saldo) VALUES(:username, :saldo)")
    Mono<Void> insert(@Param("username") String username, @Param("saldo") Long saldo);

    Mono<User> findByUsername(String username);
}

