# AWS Elastic Beanstalk Deployment Guide
# Java Spring Boot Application

## Prerequisites
1. AWS Account with Elastic Beanstalk access
2. AWS CLI installed and configured
3. Application JAR file

## Step 1: Create Application JAR

Since we had Maven timeout issues, let's use Spring Boot's built-in packaging:

```bash
# Remove problematic dependency copy plugin and build clean JAR
mvn clean compile spring-boot:repackage -DskipTests
```

## Step 2: Prepare AWS EB Configuration

### Create .ebextensions folder for AWS configuration
The .ebextensions folder contains configuration files for EB deployment.

### Environment Properties:
- **Platform**: Java 17/21 with Corretto
- **Instance**: t3.micro (free tier eligible)
- **Load Balancer**: Application Load Balancer
- **Health Checks**: /actuator/health

## Step 3: Deploy to Elastic Beanstalk

### Option A: EB CLI (Recommended)
```bash
eb init --platform java-17
eb create hf-gateway-prod
eb deploy
```

### Option B: AWS Console
1. Go to AWS Elastic Beanstalk Console
2. Create Application
3. Upload JAR file
4. Configure environment

## Step 4: Key Advantages over Render

✅ **Better Java Support**: Native Java platform support
✅ **No Sleep Issues**: Always-on applications
✅ **Custom Ports**: Can configure TCP ports 10010/10011
✅ **Scalability**: Auto-scaling capabilities  
✅ **Monitoring**: CloudWatch integration
✅ **Free Tier**: 750 hours/month free

## Step 5: Expected URL Format
```
http://hf-gateway-prod.region.elasticbeanstalk.com
```

Let's proceed with creating the EB configuration!