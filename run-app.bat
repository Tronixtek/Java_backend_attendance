@echo off
REM HF TCP Gateway - Local Deployment Script (No Docker)

echo 🚀 Starting HF TCP Gateway Application...
echo.

REM Check if Java is available
java -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ❌ Java is not installed or not in PATH
    echo Please install Java 21 or later
    pause
    exit /b 1
)

echo ✅ Java detected

REM Check if Maven is available
mvn -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ❌ Maven is not installed or not in PATH
    echo Please install Maven
    pause
    exit /b 1
)

echo ✅ Maven detected
echo.

REM Set the working directory
cd /d "%~dp0"

echo 📁 Current directory: %cd%
echo.

echo 🔨 Starting application with Spring Boot...
echo.
echo 📍 Application will be available at:
echo    🌐 Web API: http://localhost:8081
echo    🔌 Gateway TCP: localhost:10010
echo    🔌 SDK TCP: localhost:10011
echo.
echo 📝 To stop the application, press Ctrl+C
echo ⏳ Starting application...
echo.

REM Run the Spring Boot application
mvn spring-boot:run

echo.
echo 🛑 Application stopped
pause