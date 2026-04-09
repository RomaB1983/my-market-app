package ru.yandex.practicum.shop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession;

@Configuration
@EnableRedisWebSession(redisNamespace = "shop:session")
public class SessionConfig {
}