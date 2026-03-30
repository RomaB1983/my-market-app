package ru.yandex.practicum.payment.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.yandex.practicum.payment.model.User;

public interface UserRepository extends ReactiveCrudRepository<User, String>{
}

