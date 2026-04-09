# Используем официальный образ OpenJDK 21
FROM eclipse-temurin:21-jre-jammy

# Создаём директорию для приложения
WORKDIR /app

# Копируем собранный JAR‑файл (предполагается, что он лежит в target/)
COPY target/shop-service-0.0.1-SNAPSHOT.jar app.jar

# Открываем порт 8080
EXPOSE 8080

# Запускаем приложение
CMD ["java", "-jar", "app.jar"]
