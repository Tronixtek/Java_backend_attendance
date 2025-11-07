@echo off
REM HF TCP Gateway - Quick Start Script (Cloud Ready)

title HF TCP Gateway - Cloud Ready

echo ==========================================
echo     🚀 HF TCP Gateway - Cloud Ready
echo ==========================================
echo.
echo This application is configured for:
echo ✅ Local development and testing
echo ✅ Cloud deployment (Heroku, Railway, etc.)
echo ✅ Remote device communication
echo ✅ CORS enabled for web integration
echo.

if "%1"=="start" goto start
if "%1"=="test" goto test
if "%1"=="info" goto info
if "%1"=="help" goto help
goto menu

:menu
echo What would you like to do?
echo.
echo [1] Start Application
echo [2] Test API Endpoints  
echo [3] View Application Info
echo [4] Help
echo [5] Exit
echo.
set /p choice="Enter your choice (1-5): "

if "%choice%"=="1" goto start
if "%choice%"=="2" goto test
if "%choice%"=="3" goto info
if "%choice%"=="4" goto help
if "%choice%"=="5" goto exit
goto menu

:start
echo.
echo 🚀 Starting HF TCP Gateway...
echo.
call run-app.bat
goto end

:test
echo.
echo 🧪 Testing API endpoints...
echo.
echo Testing health endpoint...
timeout /t 2 >nul
curl -s http://localhost:8081/actuator/health
echo.
echo.
echo Testing application info...
timeout /t 1 >nul
curl -s http://localhost:8081/api/info
echo.
echo.
echo Testing application status...
timeout /t 1 >nul  
curl -s http://localhost:8081/api/status
echo.
echo.
echo ✅ API tests completed!
echo.
pause
goto menu

:info
echo.
echo 📋 Application Information:
echo.
echo 🌐 Web API: http://localhost:8081
echo 📡 Gateway TCP: localhost:10010
echo 📡 SDK TCP: localhost:10011
echo.
echo 🔗 Available Endpoints:
echo   GET  /actuator/health    - Health check
echo   GET  /api/info          - Application info
echo   GET  /api/status        - Application status
echo   POST /api/device/info   - Get device information
echo   POST /api/device/test   - Test device connection
echo.
echo 📱 Device Configuration:
echo   Host: your-server-ip (or localhost for testing)
echo   Port: 10010
echo   Secret: 123456
echo.
echo 🌍 Cloud Deployment Ready:
echo   ✅ CORS enabled for web integration
echo   ✅ Health checks configured
echo   ✅ Environment variables supported
echo   ✅ Remote access configured
echo.
pause
goto menu

:help
echo.
echo 🆘 Help - HF TCP Gateway
echo.
echo Usage: start.bat [command]
echo.
echo Commands:
echo   start  - Start the application
echo   test   - Test API endpoints
echo   info   - Show application information
echo   help   - Show this help
echo.
echo Manual Usage:
echo   1. Run 'run-app.bat' to start the application
echo   2. Open browser to http://localhost:8081/actuator/health
echo   3. Configure your device to connect to port 10010
echo   4. Test device communication via API
echo.
echo Troubleshooting:
echo   - Ensure Java 21+ is installed
echo   - Ensure ports 8081, 10010, 10011 are available
echo   - Check firewall settings for device connections
echo   - Use curl or Postman to test API endpoints
echo.
pause
goto menu

:exit
echo.
echo 👋 Goodbye!
exit /b 0

:end