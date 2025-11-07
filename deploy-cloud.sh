#!/bin/bash
# Cloud deployment script for HF TCP Gateway

echo "=============================================="
echo "Building HF TCP Gateway for Cloud Deployment"
echo "=============================================="

# Install local dependency
echo "Installing local HF Gateway dependency..."
mvn install:install-file -Dfile=lib/hf-tcp-gateway-1.0.0.jar -DgroupId=com.hfims -DartifactId=hf-tcp-gateway -Dversion=1.0.0 -Dpackaging=jar

# Build the application
echo "Building application..."
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo "=============================================="
    echo "Build completed successfully!"
    echo "=============================================="
    echo ""
    echo "Generated files:"
    echo "- JAR: target/hf-tcp-gateway-demo-1.0.0.jar"
    echo "- Dockerfile: Dockerfile"
    echo ""
    echo "=============================================="
    echo "Cloud Deployment Instructions:"
    echo "=============================================="
    echo ""
    echo "Option 1: Direct JAR Deployment"
    echo "1. Upload JAR to your cloud server"
    echo "2. Run: java -jar hf-tcp-gateway-demo-1.0.0.jar"
    echo "3. Configure firewall for ports: 8081, 10010, 10011"
    echo ""
    echo "Option 2: Docker Deployment"
    echo "1. Build image: docker build -t hf-gateway ."
    echo "2. Run container: docker run -p 8081:8081 -p 10010:10010 -p 10011:10011 hf-gateway"
    echo ""
    echo "=============================================="
    echo "Required Firewall Configuration:"
    echo "=============================================="
    echo "- Port 8081: REST API (HTTP) - Allow from 0.0.0.0/0"
    echo "- Port 10010: Gateway TCP - Allow from device networks"
    echo "- Port 10011: SDK TCP - Internal communication"
    echo ""
    echo "=============================================="
    echo "Update Your MERN App:"
    echo "=============================================="
    echo "Update API URL to: http://YOUR_CLOUD_SERVER_IP:8081/api"
    echo ""
    echo "Test endpoints:"
    echo "- Health: GET http://YOUR_CLOUD_SERVER_IP:8081/api/health"
    echo "- Info: GET http://YOUR_CLOUD_SERVER_IP:8081/api/info"
    echo "- Test Device: POST http://YOUR_CLOUD_SERVER_IP:8081/api/test"
else
    echo "=============================================="
    echo "Build failed! Please check the errors above."
    echo "=============================================="
    exit 1
fi