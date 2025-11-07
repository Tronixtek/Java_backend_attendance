# Multi-stage Docker build for HF TCP Gateway
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build

# Set working directory
WORKDIR /app

# Copy local JAR dependencies first
COPY lib/ ./lib/

# Install the local hf-tcp-gateway JAR to Maven local repository
RUN mvn install:install-file \
    -Dfile=./lib/hf-tcp-gateway-1.0.0.jar \
    -DgroupId=com.hfims.boot \
    -DartifactId=hf-tcp-gateway \
    -Dversion=1.0.0 \
    -Dpackaging=jar

# Copy pom.xml and download dependencies (for better caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Set metadata
LABEL maintainer="HF TCP Gateway Demo"
LABEL description="HF TCP Gateway Demo Application with Cloud Support"
LABEL version="1.0.0"

# Install required packages
RUN apk add --no-cache \
    tzdata \
    curl \
    && rm -rf /var/cache/apk/*

# Set timezone
ENV TZ=UTC

# Create application user for security
RUN addgroup -g 1001 appgroup && \
    adduser -D -u 1001 -G appgroup appuser

# Set working directory
WORKDIR /app

# Copy the JAR file from build stage
COPY --from=build /app/target/hf-tcp-gateway-demo-*.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs && \
    chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose ports
# 8081 - Web/REST API port
# 10010 - Gateway TCP port  
# 10011 - SDK TCP port
EXPOSE 8081 10010 10011

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# JVM configuration for container environment
ENV JAVA_OPTS="-Xms512m -Xmx1024m \
    -XX:+UseG1GC \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=docker"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]