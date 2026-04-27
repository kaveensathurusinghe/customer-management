# Build stage
FROM maven:3.8.4-openjdk-8-slim AS build
WORKDIR /app
# Copy only the pom.xml to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage - Use Amazon Corretto 8 Alpine (supports ARM64/Apple Silicon)
FROM amazoncorretto:8-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Add a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]


