# Stage 1: Build the application
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app

# Copy maven wrapper and pom.xml first to leverage cache
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Copy frontend files to static resources directory
RUN mkdir -p ./src/main/resources/static/frontend
COPY frontend/*.html frontend/*.css frontend/*.js frontend/*.md ./src/main/resources/static/frontend/

# Build the application
RUN ./mvnw clean package -DskipTests
#RUN ./mvnw clean package -Dtest=ResilienceIntegrationTest
#RUN ./mvnw clean package

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
