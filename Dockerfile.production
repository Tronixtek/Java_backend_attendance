# Multi-stage Docker build for production deployment
# Stage 1: Build the application
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Install required packages for build
RUN apk add --no-cache curl

# Copy local JAR dependencies
COPY lib/ ./lib/

# Install the local hf-tcp-gateway JAR to Maven local repository
RUN mvn install:install-file \
    -Dfile=./lib/hf-tcp-gateway-1.0.0.jar \
    -DgroupId=com.hfims.boot \
    -DartifactId=hf-tcp-gateway \
    -Dversion=1.0.0 \
    -Dpackaging=jar

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime container
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install runtime dependencies
RUN apk add --no-cache \
    tzdata \
    curl \
    && rm -rf /var/cache/apk/*

# Create logs directory and app user
RUN mkdir -p /app/logs && \
    adduser -D -s /bin/sh appuser && \
    chown -R appuser:appuser /app

# Copy the JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Change to app user for security
USER appuser

# Set timezone
ENV TZ=UTC

# Expose ports
EXPOSE 8081 10010 10011

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# JVM configuration for production
ENV JAVA_OPTS="-Xms256m -Xmx512m \
    -XX:+UseG1GC \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -Djava.security.egd=file:/dev/./urandom \
    -Dserver.port=8081 \
    -Dspring.profiles.active=production"

# Run the JAR file
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]