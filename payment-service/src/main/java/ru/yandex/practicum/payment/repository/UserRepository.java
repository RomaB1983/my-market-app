package ru.yandex.practicum.payment.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.model.User;

public interface UserRepository extends ReactiveCrudRepository<User, String>{
    @Query(value = "INSERT INTO users(user_id, saldo) VALUES(:id, :saldo)")
    Mono<Void> insert(@Param("id") String userId,@Param("saldo") Long saldo);
}

