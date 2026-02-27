# Используем официальный образ OpenJDK 21
FROM eclipse-temurin:21-jre-jammy

# Создаём директорию для приложения
WORKDIR /app

# Копируем собранный WAR‑файл (предполагается, что он лежит в target/)
COPY target/my-market-app-0.0.1-SNAPSHOT.jar /app/app.war

# Открываем порт 8080
EXPOSE 8080

# Запускаем приложение
CMD ["java", "-jar", "app.war"]
