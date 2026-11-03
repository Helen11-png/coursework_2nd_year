# Образ с Maven и JDK 21. "AS build" даёт этапу имя, чтобы взять из него результат.
FROM maven:3.9-eclipse-temurin-21 AS build

# Рабочая папка внутри контейнера. Все следующие команды выполняются в ней.
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
# -DskipTests: тесты запускаются отдельно в CI (им нужен Docker для Testcontainers,а внутри docker build его нет).
COPY src ./src
RUN mvn -B -q package -DskipTests
# Лёгкий образ только с JRE (без компилятора и Maven).
FROM eclipse-temurin:21-jre

WORKDIR /app

# Отдельный пользователь без прав администратора:
RUN useradd --system --no-create-home appuser
COPY --from=build /app/target/*.jar app.jar

# Дальше всё запускается от appuser.
USER appuser

# Ограничиваем память JVM: берём до 75% памяти контейнера, иначе Java может занять всё и контейнер будет убит системой. JAVA_TOOL_OPTIONS читается JVM автоматически.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

# Документация: приложение слушает порт 8080 (реально порт открывает docker-compose).
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
