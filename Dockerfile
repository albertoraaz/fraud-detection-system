# We only need the Runtime Stage because Jenkins already ran Maven
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Security: Run as non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the JAR that was built by the Jenkins 'Build & Package' stage
COPY target/*.jar app.jar

EXPOSE 8081

# Optimized for Cloud/K8s environments
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]