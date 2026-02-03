# Stage 1: Сборка
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY . .

# Добавляем права на выполнение
RUN chmod +x gradlew

RUN ./gradlew clean bootJar --no-daemon

# Stage 2: Рантайм
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]