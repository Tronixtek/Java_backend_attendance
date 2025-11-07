# HF TCP Gateway - Docker Deployment Guide

## Overview

This guide provides instructions for containerizing and deploying the HF TCP Gateway application using Docker. The application includes a Spring Boot REST API and TCP gateway services for device communication.

## Architecture

The containerized application exposes three main ports:
- **8081**: REST API for device management and testing
- **10010**: TCP Gateway port for device connections  
- **10011**: SDK TCP port for internal communication

## Prerequisites

- Docker Desktop installed and running
- At least 2GB of available RAM
- Ports 8081, 10010, and 10011 available on your host

## Quick Start

### Option 1: Using Docker Compose (Recommended)

```bash
# Build and start the application
docker-compose up -d

# View logs
docker-compose logs -f

# Stop the application
docker-compose down
```

### Option 2: Using Build Scripts

**Windows:**
```cmd
# Build and run
docker-build.bat --run

# Or with Docker Compose
docker-build.bat --compose
```

**Linux/Mac:**
```bash
# Make script executable
chmod +x docker-build.sh

# Build and run
./docker-build.sh --run

# Or with Docker Compose  
./docker-build.sh --compose
```

### Option 3: Manual Docker Commands

```bash
# Build the image
docker build -t hf-tcp-gateway:latest .

# Run the container
docker run -d \
  --name hf-tcp-gateway-demo \
  --restart unless-stopped \
  -p 8081:8081 \
  -p 10010:10010 \
  -p 10011:10011 \
  -v ./logs:/app/logs \
  hf-tcp-gateway:latest
```

## Container Management

Use the provided management script for common operations:

```cmd
# Windows
docker-manage.bat [command]

# Available commands:
docker-manage.bat build     # Build the image
docker-manage.bat run       # Start container
docker-manage.bat stop      # Stop container
docker-manage.bat restart   # Restart container
docker-manage.bat logs      # View logs
docker-manage.bat status    # Check status
docker-manage.bat shell     # Access container shell
docker-manage.bat clean     # Remove container and image
```

## Configuration

### Environment Variables

The container supports the following environment variables:

- `SPRING_PROFILES_ACTIVE`: Set to "docker" by default
- `SERVER_ADDRESS`: Server bind address (default: 0.0.0.0)
- `LOGGING_LEVEL_ROOT`: Root logging level (default: INFO)
- `LOGGING_LEVEL_COM_HFIMS`: Application logging level (default: DEBUG)
- `TZ`: Timezone setting (default: UTC)

### Volume Mounts

- `/app/logs`: Application logs directory
- `/etc/localtime`: System timezone (read-only)

## API Endpoints

Once the container is running, the following endpoints are available:

### Health Check
```
GET http://localhost:8081/actuator/health
```

### Device Management
```
POST http://localhost:8081/api/device/info
Content-Type: application/json
{
  "deviceKey": "020e7096a03c670f63",
  "secret": "123456"
}

POST http://localhost:8081/api/device/test  
Content-Type: application/json
{
  "deviceKey": "020e7096a03c670f63",
  "secret": "123456"
}
```

### Application Info
```
GET http://localhost:8081/api/info
GET http://localhost:8081/api/status
```

## Device Configuration

To connect your HF device to the containerized gateway:

1. Ensure your device can reach the Docker host IP
2. Configure device connection settings:
   - **Gateway Host**: `<docker-host-ip>`
   - **Gateway Port**: `10010`
   - **Secret**: `123456` (or your configured secret)

## Monitoring and Logs

### View Container Logs
```bash
# Real-time logs
docker logs -f hf-tcp-gateway-demo

# Or with Docker Compose
docker-compose logs -f
```

### Access Log Files
```bash
# View application logs (mounted volume)
cat ./logs/hf-gateway.log

# Access container filesystem
docker exec -it hf-tcp-gateway-demo /bin/sh
```

### Health Monitoring
```bash
# Check container health
docker ps

# Check application health
curl http://localhost:8081/actuator/health

# View metrics
curl http://localhost:8081/actuator/metrics
```

## Security Considerations

The Docker image implements several security best practices:

- **Non-root user**: Application runs as user `appuser` (UID 1001)
- **Minimal base image**: Uses Alpine Linux for smaller attack surface
- **Health checks**: Built-in health monitoring
- **Resource limits**: Memory and CPU limits configured
- **Read-only volumes**: System files mounted read-only

## Troubleshooting

### Common Issues

1. **Port conflicts**: Ensure ports 8081, 10010, 10011 are not in use
   ```bash
   netstat -an | findstr "8081\|10010\|10011"
   ```

2. **Container won't start**: Check Docker logs
   ```bash
   docker logs hf-tcp-gateway-demo
   ```

3. **Device connection issues**: Verify network connectivity
   ```bash
   # Test from device to Docker host
   telnet <docker-host-ip> 10010
   ```

4. **Memory issues**: Increase Docker memory allocation
   ```bash
   # Check memory usage
   docker stats hf-tcp-gateway-demo
   ```

### Debug Mode

To run the container with debug logging:

```bash
docker run -d \
  --name hf-tcp-gateway-demo \
  -p 8081:8081 -p 10010:10010 -p 10011:10011 \
  -e LOGGING_LEVEL_ROOT=DEBUG \
  -e LOGGING_LEVEL_COM_HFIMS=TRACE \
  hf-tcp-gateway:latest
```

## Production Deployment

For production environments, consider:

1. **Use external volumes** for persistent logging
2. **Configure proper networking** (bridge/overlay networks)
3. **Set resource limits** based on expected load
4. **Use secrets management** for sensitive configuration
5. **Implement proper backup** strategies for logs and data
6. **Set up monitoring** with Prometheus/Grafana

### Production Docker Compose Example

```yaml
version: '3.8'
services:
  hf-gateway:
    image: hf-tcp-gateway:latest
    restart: always
    ports:
      - "8081:8081"
      - "10010:10010" 
      - "10011:10011"
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - LOGGING_LEVEL_ROOT=WARN
    volumes:
      - /var/log/hf-gateway:/app/logs
    deploy:
      resources:
        limits:
          memory: 2G
          cpus: '2.0'
        reservations:
          memory: 1G
          cpus: '1.0'
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8081/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
```

## Support

For issues related to Docker deployment:
1. Check the application logs in `./logs/hf-gateway.log`
2. Verify Docker container status with `docker ps -a`
3. Test API endpoints manually using curl or Postman
4. Check device connectivity to the exposed ports