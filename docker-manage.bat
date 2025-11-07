@echo off
REM Docker Management Script for HF TCP Gateway

set CONTAINER_NAME=hf-tcp-gateway-demo
set IMAGE_NAME=hf-tcp-gateway:latest

if "%1"=="build" goto build
if "%1"=="run" goto run
if "%1"=="stop" goto stop
if "%1"=="restart" goto restart
if "%1"=="logs" goto logs
if "%1"=="status" goto status
if "%1"=="shell" goto shell
if "%1"=="clean" goto clean
if "%1"=="help" goto help
goto help

:build
echo 🔨 Building Docker image...
docker build -t %IMAGE_NAME% .
echo ✅ Build complete!
goto end

:run
echo 🚀 Starting container...
docker stop %CONTAINER_NAME% >nul 2>&1
docker rm %CONTAINER_NAME% >nul 2>&1
if not exist logs mkdir logs
docker run -d ^
    --name %CONTAINER_NAME% ^
    --restart unless-stopped ^
    -p 8081:8081 ^
    -p 10010:10010 ^
    -p 10011:10011 ^
    -v "%cd%/logs:/app/logs" ^
    %IMAGE_NAME%
echo ✅ Container started!
echo 📍 Web API: http://localhost:8081
echo 📍 Gateway TCP: localhost:10010
echo 📍 SDK TCP: localhost:10011
goto end

:stop
echo 🛑 Stopping container...
docker stop %CONTAINER_NAME%
echo ✅ Container stopped!
goto end

:restart
echo 🔄 Restarting container...
docker restart %CONTAINER_NAME%
echo ✅ Container restarted!
goto end

:logs
echo 📝 Container logs:
docker logs -f %CONTAINER_NAME%
goto end

:status
echo 📊 Container status:
docker ps -a --filter "name=%CONTAINER_NAME%"
echo.
echo 🏥 Health check:
curl -s http://localhost:8081/actuator/health
goto end

:shell
echo 🐚 Opening shell in container...
docker exec -it %CONTAINER_NAME% /bin/sh
goto end

:clean
echo 🧹 Cleaning up...
docker stop %CONTAINER_NAME% >nul 2>&1
docker rm %CONTAINER_NAME% >nul 2>&1
docker rmi %IMAGE_NAME% >nul 2>&1
echo ✅ Cleanup complete!
goto end

:help
echo 🐳 HF TCP Gateway Docker Manager
echo.
echo Usage: docker-manage.bat [command]
echo.
echo Commands:
echo   build     - Build the Docker image
echo   run       - Run the container
echo   stop      - Stop the container
echo   restart   - Restart the container
echo   logs      - Show container logs
echo   status    - Show container status and health
echo   shell     - Open shell in container
echo   clean     - Stop and remove container and image
echo   help      - Show this help
echo.
echo Examples:
echo   docker-manage.bat build
echo   docker-manage.bat run
echo   docker-manage.bat logs
goto end

:end