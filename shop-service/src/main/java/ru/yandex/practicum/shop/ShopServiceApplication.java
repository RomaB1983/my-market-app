package ru.yandex.practicum.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession;

@SpringBootApplication
@EnableR2dbcRepositories
@EnableRedisWebSession(redisNamespace = "shop:session")
public class ShopServiceApplication {
    public static void main(String[] args) {
         SpringApplication.run(ShopServiceApplication.class, args);
    }
}
