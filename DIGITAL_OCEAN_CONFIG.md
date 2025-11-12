# Digital Ocean Deployment Configuration
# Use this for your Digital Ocean App Platform environment variables

# ====== PRODUCTION ENVIRONMENT VARIABLES ======

# Server Configuration
PORT=8081
SPRING_PROFILES_ACTIVE=production
SERVER_IP=143.198.150.26

# XO5 Device Gateway Configuration
DEVICE_IP=143.198.150.26
DEVICE_PORT=10011
DEVICE_TIMEOUT=10000
DEVICE_USERNAME=admin
DEVICE_PASSWORD=123456

# MERN Backend Integration (Update with your MERN app URL)
MERN_BACKEND_URL=https://your-mern-backend.ondigitalocean.app
MERN_BACKEND_TIMEOUT=10000
MERN_RETRY_ATTEMPTS=3
MERN_RETRY_DELAY=1000

# Application Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_GATEWAY=DEBUG

# ====== IMPORTANT NOTES ======
# 1. Your server IP: 143.198.150.26
# 2. Device username: admin (updated)
# 3. Configure XO5 device to connect to: 143.198.150.26:10010
# 4. Test endpoints at: https://your-app.ondigitalocean.app