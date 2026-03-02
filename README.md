# Spring Boot -приложение my-market-app реализует веб-приложение «Витрина интернет-магазина».

## Приложение построено по Шаблону проектирования «Controller → Service → Repository»

- Hibernate -работа с БД (БД H2)
- Thymeleaf - взаимодейстивие с HTML шаблонами

## Используемые технологии
- Java 21
- Spring Boot
- Spring Boot Test
- JPA
- Thymeleaf
- JUnit 5
- Mockito
- Maven

## Структура проекта
- controller — контроллеры для работы с продуктами, корзиной и заказами
- dto - объекты для передачи запросов/ответов
- model — доменные модели
- service — бизнес-логика
- repository — работа с БД 

## Настройки
`src/main/resources/application.properties`

## Сборка JAR файла
mvn clean package

## Запуск тестов
mvn test

## Dockerfile
DockerFile для упаковки приложения в Docker-контейнер