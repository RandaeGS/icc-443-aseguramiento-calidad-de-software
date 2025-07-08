FROM maven:3.9.10-amazoncorretto-21
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests
EXPOSE 8080
# Ejecuta desde el directorio quarkus-app
CMD ["java", "-jar", "target/quarkus-app/quarkus-run.jar"]
