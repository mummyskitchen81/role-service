#Stage 1: Build with Java 21
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Build project, skip tests
RUN chmod +x mvnw && \
      ./mvnw dependency:go-offline -B

COPY src ./src

# Build project (clean package with tests skipped)
RUN ./mvnw -B -DskipTests clean package

# Stage 2: Run with Java 21 JRE
FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Start your Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]