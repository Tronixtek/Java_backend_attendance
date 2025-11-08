# Test Script for Java Backend Deployment
# Base URL: https://java-backend-attendance.onrender.com

Write-Host "🚀 Testing Java Backend Deployment" -ForegroundColor Green
Write-Host "Base URL: https://java-backend-attendance.onrender.com" -ForegroundColor Cyan
Write-Host ""

# Function to test endpoint
function Test-Endpoint {
    param($url, $name)
    Write-Host "Testing $name..." -ForegroundColor Yellow
    Write-Host "URL: $url" -ForegroundColor Gray
    
    try {
        $response = Invoke-WebRequest -Uri $url -Method Get -TimeoutSec 30
        Write-Host "✅ SUCCESS - Status: $($response.StatusCode)" -ForegroundColor Green
        Write-Host "Response: $($response.Content)" -ForegroundColor White
    }
    catch {
        Write-Host "❌ FAILED - Error: $($_.Exception.Message)" -ForegroundColor Red
    }
    Write-Host "----------------------------------------" -ForegroundColor Gray
}

# Test endpoints
$baseUrl = "https://java-backend-attendance.onrender.com"

Test-Endpoint "$baseUrl/actuator/health" "Health Check"
Test-Endpoint "$baseUrl/api/status" "App Status"
Test-Endpoint "$baseUrl/api/device/info" "Device Info"

# Test with simple root endpoint
Test-Endpoint "$baseUrl/" "Root Endpoint"

Write-Host "🏁 Testing Complete" -ForegroundColor Green