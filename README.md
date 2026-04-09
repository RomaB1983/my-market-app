# Spring Boot - приложение my-market-app мультимодульное приложение, содержит два сервиса и OpenApi схему взаимодействия:
#    1. Веб-сервис «Витрина интернет-магазина» (shop-service)
#    2. Сервис оплаты товаров в корзине(payment-service)
#    3. OpenApi спецификация (api-spec)   


## Приложение построено по Шаблону проектирования «Controller → Service → Repository»

- Rd2BC -работа с БД (БД H2)
- Thymeleaf - взаимодейстивие с HTML шаблонами
- Redis  - кеш для хранения товаров

## Используемые технологии
- Java 21
- Spring Boot
- Spring Boot Test
- RD2BC
- WebFlux
- Redis (reactive)
- Thymeleaf
- JUnit 5
- Mockito
- Maven

## Структура сервисов
- controller — контроллеры для работы с продуктами, корзиной и заказами
- dto - объекты для передачи запросов/ответов
- model — доменные модели
- service — бизнес-логика
- repository — работа с БД 

## Сборка

### Собрать весь мультипроект
```bash
mvn clean install
```

### Собрать только витрину
```bash
mvn clean install -pl shop-service -am
```

### Собрать только payment-service
```bash
mvn clean install -pl payment-service -am
```
## API сервиса

OpenAPI схема: `api-spec/src/main/resources/payment-api.yaml`

### Получить баланс
```http
GET /api/balance
Header UserId
```

### Осуществить платёж
```http
POST /api/pay
Content-Type: application/json
Header UserId
{
  "amount": 100
}
```