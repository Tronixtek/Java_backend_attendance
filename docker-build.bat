@echo off
REM HF TCP Gateway Docker Build and Run Script for Windows

echo 🐳 Building HF TCP Gateway Docker Image...

REM Build the Docker image
docker build -t hf-tcp-gateway:latest .

if %ERRORLEVEL% neq 0 (
    echo ❌ Docker build failed!
    exit /b 1
)

echo ✅ Docker image built successfully!

REM Check if we should run the container
if "%1"=="--run" goto run
if "%1"=="-r" goto run
if "%1"=="--compose" goto compose
if "%1"=="-c" goto compose
goto end

:run
echo 🚀 Starting HF TCP Gateway container...

REM Stop and remove existing container if it exists
docker stop hf-tcp-gateway-demo >nul 2>&1
docker rm hf-tcp-gateway-demo >nul 2>&1

REM Create logs directory if it doesn't exist
if not exist logs mkdir logs

REM Run the container
docker run -d ^
    --name hf-tcp-gateway-demo ^
    --restart unless-stopped ^
    -p 8081:8081 ^
    -p 10010:10010 ^
    -p 10011:10011 ^
    -v "%cd%/logs:/app/logs" ^
    hf-tcp-gateway:latest

if %ERRORLEVEL% neq 0 (
    echo ❌ Failed to start container!
    exit /b 1
)

echo ✅ Container started successfully!
echo 📝 Container logs:
docker logs -f hf-tcp-gateway-demo
goto end

:compose
echo 🚀 Starting with Docker Compose...
docker-compose up -d

if %ERRORLEVEL% neq 0 (
    echo ❌ Docker Compose failed!
    exit /b 1
)

echo ✅ Services started with Docker Compose!
echo 📝 Service logs:
docker-compose logs -f
goto end

:end
if "%1"=="" (
    echo ✅ Build complete! To run the container, use:
    echo    docker-build.bat --run     ^(or -r^)
    echo    docker-build.bat --compose ^(or -c^)
    echo.
    echo 📝 Manual run command:
    echo    docker run -d --name hf-tcp-gateway-demo -p 8081:8081 -p 10010:10010 -p 10011:10011 hf-tcp-gateway:latest
)