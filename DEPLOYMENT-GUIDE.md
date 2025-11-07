# 🚀 HF TCP Gateway - Non-Docker Deployment Guide

## Overview

Your HF TCP Gateway application is ready for deployment without Docker! This guide covers local deployment and cloud deployment options.

## ⚡ Quick Start (Local)

### Option 1: Interactive Menu
```cmd
start.bat
```
Choose option [1] to start the application.

### Option 2: Direct Start
```cmd
run-app.bat
```

### Option 3: Maven Command
```cmd
mvn spring-boot:run
```

## 📋 Prerequisites

- **Java 21+** installed and in PATH
- **Maven 3.6+** installed and in PATH
- Ports **8081**, **10010**, **10011** available

## 🌐 Application Endpoints

Once running, your application provides:

### Health & Info
- **Health Check**: `GET http://localhost:8081/actuator/health`
- **App Info**: `GET http://localhost:8081/api/info`
- **App Status**: `GET http://localhost:8081/api/status`

### Device Management
```bash
# Get device information
POST http://localhost:8081/api/device/info
Content-Type: application/json
{
  "deviceKey": "020e7096a03c670f63",
  "secret": "123456"
}

# Test device connection
POST http://localhost:8081/api/device/test
Content-Type: application/json
{
  "deviceKey": "020e7096a03c670f63", 
  "secret": "123456"
}
```

## 📱 Device Configuration

Configure your HF device to connect to:
- **Host**: `localhost` (for local) or `your-server-ip` (for cloud)
- **Gateway Port**: `10010`
- **Secret**: `123456`

## ☁️ Cloud Deployment Options

### 1. Heroku Deployment

#### Setup Steps:
```bash
# Install Heroku CLI
# Create Heroku app
heroku create your-app-name

# Set Java version
heroku config:set JAVA_VERSION=21

# Deploy
git add .
git commit -m "Deploy to Heroku"
git push heroku main
```

#### Configuration:
- ✅ `Procfile` already created
- ✅ `application-heroku.yml` configured
- ✅ Environment variables supported

### 2. Railway Deployment

#### Setup Steps:
```bash
# Install Railway CLI
npm install -g @railway/cli

# Login to Railway
railway login

# Create project
railway init
railway link

# Deploy
railway up
```

#### Configuration:
- ✅ `railway.yml` already created
- ✅ Java 21 and Maven configured
- ✅ All ports exposed

### 3. Render Deployment

#### Setup Steps:
1. Connect your GitHub repository to Render
2. Create a new Web Service
3. Use these settings:
   - **Build Command**: `mvn clean compile`
   - **Start Command**: `mvn spring-boot:run`
   - **Environment**: Java 21

### 4. Digital Ocean App Platform

#### Setup Steps:
1. Create new app from GitHub
2. Configure build settings:
   - **Build Command**: `mvn clean compile`
   - **Run Command**: `mvn spring-boot:run`
   - **Environment Variables**: `JAVA_VERSION=21`

### 5. Google Cloud Run

#### Setup Steps:
```bash
# Deploy from source
gcloud run deploy hf-gateway \
  --source . \
  --platform managed \
  --region us-central1 \
  --port 8081
```

## 🔧 Environment Variables

For cloud deployment, set these environment variables:

```bash
# Required
JAVA_VERSION=21
PORT=8081
SERVER_ADDRESS=0.0.0.0

# Optional
GATEWAY_PORT=10010
SDK_PORT=10011
SPRING_PROFILES_ACTIVE=production
```

## 🛡️ Security Configuration

### Firewall Rules (Cloud)
- **Inbound**: Allow ports 8081 (HTTP), 10010 (Gateway), 10011 (SDK)
- **Outbound**: Allow all

### Application Security
- ✅ CORS enabled for web integration
- ✅ Health checks configured
- ✅ Secure headers configured

## 📊 Monitoring

### Local Monitoring
```bash
# Check application status
curl http://localhost:8081/actuator/health

# View application info
curl http://localhost:8081/api/info

# Test device endpoints
curl -X POST http://localhost:8081/api/device/test \
  -H "Content-Type: application/json" \
  -d '{"deviceKey":"020e7096a03c670f63","secret":"123456"}'
```

### Cloud Monitoring
- Use platform-specific monitoring (Heroku Logs, Railway Logs, etc.)
- Set up health check endpoints: `/actuator/health`
- Monitor application metrics: `/actuator/metrics`

## 🆘 Troubleshooting

### Common Issues

#### 1. Application Won't Start
```bash
# Check Java version
java -version

# Check Maven version  
mvn -version

# Check port availability
netstat -an | findstr "8081"
```

#### 2. Device Connection Issues
```bash
# Test TCP connectivity
telnet localhost 10010

# Check firewall settings
# Verify device configuration
```

#### 3. API Not Responding
```bash
# Check application logs
# Verify CORS configuration
# Test with curl or Postman
```

#### 4. Cloud Deployment Issues
- **Heroku**: Check build logs with `heroku logs --tail`
- **Railway**: Use Railway dashboard logs
- **Render**: Check deployment logs in dashboard

### Debug Mode

To run with debug logging:
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dlogging.level.com.hfims=DEBUG"
```

## 🌍 MERN Integration

Your MERN application can connect to the deployed gateway:

```javascript
// Update the API base URL in your MERN app
const API_BASE_URL = 'https://your-app.herokuapp.com'; // or your cloud URL

// Use the cloudDeviceService.js file provided
import { CloudDeviceService } from './cloudDeviceService.js';

const deviceService = new CloudDeviceService(API_BASE_URL);
```

## 📈 Scaling Considerations

### Horizontal Scaling
- Use cloud platform auto-scaling features
- Configure load balancers for multiple instances
- Ensure session stickiness for TCP connections

### Performance Optimization
- Configure JVM memory settings for cloud
- Use connection pooling for device connections
- Implement caching for frequent requests

## 🔄 CI/CD Setup

### GitHub Actions (example)
```yaml
name: Deploy to Cloud
on:
  push:
    branches: [main]
jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '21'
      - run: mvn clean compile
      - run: # Deploy to your chosen platform
```

## 📚 Next Steps

1. **Local Testing**: Run `start.bat` and test all endpoints
2. **Device Setup**: Configure your device to connect to localhost:10010
3. **Cloud Deploy**: Choose a platform and follow deployment steps
4. **MERN Integration**: Update your web app with the cloud URL
5. **Production Setup**: Configure monitoring and scaling

---

Your application is now ready for deployment! 🎉

**Files Created:**
- ✅ `start.bat` - Interactive deployment menu
- ✅ `run-app.bat` - Quick start script
- ✅ `Procfile` - Heroku configuration
- ✅ `railway.yml` - Railway configuration
- ✅ `application-heroku.yml` - Cloud-specific settings

Just run `start.bat` and select option [1] to begin! 🚀