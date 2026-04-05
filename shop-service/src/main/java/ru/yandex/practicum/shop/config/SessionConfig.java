//package ru.yandex.practicum.shop.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
//import org.springframework.data.redis.core.ReactiveRedisOperations;
//import org.springframework.data.redis.core.ReactiveRedisTemplate;
//import org.springframework.session.ReactiveMapSessionRepository;
//import org.springframework.session.ReactiveSessionRepository;
//import org.springframework.session.config.annotation.web.server.EnableSpringWebSession;
//import org.springframework.session.data.redis.ReactiveRedisSessionRepository;
//import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession;
//import org.springframework.web.server.WebSession;
//
//import java.time.Duration;
//import java.time.temporal.ChronoUnit;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Configuration
////@EnableSpringWebSession
//@EnableRedisWebSession
//public class SessionConfig {
//
//    @Bean
//    public ReactiveSessionRepository<?> reactiveSessionRepository(ReactiveRedisOperations<String, Object> ops) {
//        ReactiveRedisSessionRepository repository = new ReactiveRedisTemplate<>(ops);
//        repository.setDefaultMaxInactiveInterval(Duration.of(30, ChronoUnit.MINUTES)); // 30 минут
//        repository.setRedisKeyNamespace("myapp:sessions");
//        return repository;
//    }
//
//
////@Bean
////public ReactiveRedisConnectionFactory redisConnectionFactory() {
////    // Настройка подключения к Redis
////    return new LettuceConnectionFactory();
////}
//
////    @Bean
////    public ReactiveSessionRepository<?> reactiveSessionRepository() {
////        return new ReactiveMapSessionRepository(new ConcurrentHashMap<>());
////    }
////    @Bean
////    public LettuceConnectionFactory redisConnectionFactory() {
////        return new LettuceConnectionFactory();
////    }
//}