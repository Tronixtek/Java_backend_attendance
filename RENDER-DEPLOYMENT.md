# 🚀 Deploying HF TCP Gateway to Render

## Step-by-Step Render Deployment Guide

### 1. **Prepare Your Repository**
✅ Your code is already on GitHub: https://github.com/Tronixtek/Java_backend_attendance.git

### 2. **Deploy to Render**

#### Go to Render Dashboard:
1. Visit [render.com](https://render.com)
2. Sign up or log in with your GitHub account
3. Click "New +" → "Web Service"

#### Connect Repository:
1. Select "Build and deploy from a Git repository"
2. Connect your GitHub account if not already connected
3. Choose repository: `Tronixtek/Java_backend_attendance`
4. Click "Connect"

#### Configure Deployment Settings:
```
Name: hf-tcp-gateway (or your preferred name)
Region: Choose closest to your location
Branch: main
Root Directory: (leave blank)
Runtime: Java
Build Command: mvn clean compile
Start Command: mvn spring-boot:run
```

#### Environment Variables:
```
JAVA_VERSION=21
PORT=8081
SERVER_ADDRESS=0.0.0.0
SPRING_PROFILES_ACTIVE=production
```

#### Instance Type:
- **Free Tier**: Good for testing (spins down after inactivity)
- **Starter**: $7/month (always on, recommended for production)

### 3. **Expected Deployment Process**

Render will:
1. ✅ Clone your repository
2. ✅ Detect Java/Maven project
3. ✅ Install Java 21
4. ✅ Run `mvn clean compile`
5. ✅ Start with `mvn spring-boot:run`
6. ✅ Expose your app on HTTPS

### 4. **Your App Will Be Available At:**
```
https://your-app-name.onrender.com
```

### 5. **API Endpoints After Deployment:**
- Health: `https://your-app-name.onrender.com/actuator/health`
- Device Info: `https://your-app-name.onrender.com/api/device/info`
- Device Test: `https://your-app-name.onrender.com/api/device/test`
- App Status: `https://your-app-name.onrender.com/api/status`

### 6. **Important Notes:**

#### **Ports for Device Connection:**
⚠️ **Important**: Render's free/starter tiers only support HTTP/HTTPS traffic on port 443/80. 

For TCP ports (10010, 10011), you have two options:

**Option A: HTTP API Only (Recommended for testing)**
- Use only the REST API endpoints
- Test device operations via HTTP requests
- Configure device to use HTTP API instead of direct TCP

**Option B: Upgrade for TCP Support**
- Render Pro plan supports custom ports
- Allows TCP connections on ports 10010, 10011

### 7. **Testing Strategy:**

**Phase 1: Test HTTP API**
```bash
# Test health endpoint
curl https://your-app-name.onrender.com/actuator/health

# Test device API via HTTP
curl -X POST https://your-app-name.onrender.com/api/device/test \
  -H "Content-Type: application/json" \
  -d '{"deviceKey":"020e7096a03c670f63","secret":"123456"}'
```

**Phase 2: Device Integration**
- If device supports HTTP API → Use REST endpoints
- If device requires TCP → Consider upgrading Render plan

### 8. **Deployment Commands for Reference:**

```bash
# Your repository is already ready for Render!
# No additional commands needed - Render will handle everything
```

---

## 🎯 Next Steps:

1. **Deploy on Render** using the settings above
2. **Test HTTP endpoints** once deployed
3. **Configure your device** to use the new cloud URL
4. **Test device connectivity** 
5. **Integrate with MERN app** using the cloud URL

Would you like me to help you with the Render deployment process or would you prefer to start with a different platform?