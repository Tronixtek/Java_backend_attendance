#!/bin/bash

# HF TCP Gateway Docker Build and Run Script

set -e

echo "🐳 Building HF TCP Gateway Docker Image..."

# Build the Docker image
docker build -t hf-tcp-gateway:latest .

echo "✅ Docker image built successfully!"

# Check if we should run the container
if [ "$1" = "--run" ] || [ "$1" = "-r" ]; then
    echo "🚀 Starting HF TCP Gateway container..."
    
    # Stop and remove existing container if it exists
    docker stop hf-tcp-gateway-demo 2>/dev/null || true
    docker rm hf-tcp-gateway-demo 2>/dev/null || true
    
    # Run the container
    docker run -d \
        --name hf-tcp-gateway-demo \
        --restart unless-stopped \
        -p 8081:8081 \
        -p 10010:10010 \
        -p 10011:10011 \
        -v "$(pwd)/logs:/app/logs" \
        hf-tcp-gateway:latest
    
    echo "✅ Container started successfully!"
    echo "📝 Container logs:"
    docker logs -f hf-tcp-gateway-demo
elif [ "$1" = "--compose" ] || [ "$1" = "-c" ]; then
    echo "🚀 Starting with Docker Compose..."
    docker-compose up -d
    echo "✅ Services started with Docker Compose!"
    echo "📝 Service logs:"
    docker-compose logs -f
else
    echo "✅ Build complete! To run the container, use:"
    echo "   ./docker-build.sh --run     (or -r)"
    echo "   ./docker-build.sh --compose (or -c)"
    echo ""
    echo "📝 Manual run command:"
    echo "   docker run -d --name hf-tcp-gateway-demo -p 8081:8081 -p 10010:10010 -p 10011:10011 hf-tcp-gateway:latest"
fi