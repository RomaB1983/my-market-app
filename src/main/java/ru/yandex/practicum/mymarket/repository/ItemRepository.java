package ru.yandex.practicum.mymarket.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Item;

@Repository
public interface ItemRepository extends ReactiveSortingRepository<Item, Long> {
    Flux<Item> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String title, String description,Pageable pageable);

    Flux<Item> findAllBy(Pageable pageable);

    Mono<Long> count();

    Mono<Long> countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String search, String search1);

    Mono<Item> findById(Long id);
}
