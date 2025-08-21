# ---- Etapa 1: Build ----
FROM maven:3.9.10-amazoncorretto-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# ---- Etapa 2: Run ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/quarkus-app /app/quarkus-app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/quarkus-app/quarkus-run.jar"]