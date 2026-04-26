package ru.yandex.practicum.shop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import ru.yandex.practicum.shop.repository.UserRepository;

import java.net.URI;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public RedirectServerLogoutSuccessHandler redirectServerLogoutSuccessHandler() {
        RedirectServerLogoutSuccessHandler logoutSuccessHandler = new RedirectServerLogoutSuccessHandler();
        logoutSuccessHandler.setLogoutSuccessUrl(URI.create("/items"));
        return logoutSuccessHandler;
    }
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                // Явно разрешаем доступ к /login и / для всех
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/login").permitAll()
                        .pathMatchers(HttpMethod.GET, "/", "/items", "/items/{id}").permitAll()
                        .pathMatchers("/images/**", "/css/**", "/js/**").permitAll()
                        .anyExchange().authenticated()
                )
                // Настраиваем форму логина
                .formLogin(form -> form
                        // URL страницы логина
                        .loginPage("/login")
                        .authenticationSuccessHandler(
                                // В случае успешного логина, перенаправляем на /message
                                new RedirectServerAuthenticationSuccessHandler("/items")
                        )
                )
                // Настраиваем обработку при выходе
                .logout(logout -> logout
                        // URL страницы выхода
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(redirectServerLogoutSuccessHandler())
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable);

        return http.build();
    }
    @Bean
    public ReactiveUserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByUsername(username)
                .map(user -> User.withUsername(user.getUsername())
                        .password(user.getPassword())
                        .roles(user.getRole())
                        .build());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}