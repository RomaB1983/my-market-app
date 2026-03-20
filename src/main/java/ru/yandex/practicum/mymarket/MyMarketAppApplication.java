package ru.yandex.practicum.mymarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication
@EnableR2dbcRepositories
public class MyMarketAppApplication {
    public static void main(String[] args) {
         SpringApplication.run(MyMarketAppApplication.class, args);
    }
}


