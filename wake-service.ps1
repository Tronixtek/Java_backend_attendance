# Wake Up Service Script
# Sometimes Render free tier services need time to wake up

Write-Host "🔄 Attempting to wake up Render service..." -ForegroundColor Yellow
Write-Host "This may take 30-60 seconds for free tier services" -ForegroundColor Cyan

$baseUrl = "https://java-backend-attendance.onrender.com"

# Try multiple times with delays
for ($i = 1; $i -le 3; $i++) {
    Write-Host ""
    Write-Host "Attempt $i/3..." -ForegroundColor Yellow
    
    try {
        $response = Invoke-WebRequest -Uri "$baseUrl/actuator/health" -Method Get -TimeoutSec 60
        Write-Host "✅ SUCCESS! Service is awake!" -ForegroundColor Green
        Write-Host "Status: $($response.StatusCode)" -ForegroundColor Green
        Write-Host "Response: $($response.Content)" -ForegroundColor White
        break
    }
    catch {
        Write-Host "⏳ Service not ready yet: $($_.Exception.Message)" -ForegroundColor Red
        if ($i -lt 3) {
            Write-Host "Waiting 30 seconds before retry..." -ForegroundColor Cyan
            Start-Sleep -Seconds 30
        }
    }
}

Write-Host ""
Write-Host "If all attempts failed, check Render dashboard logs for errors." -ForegroundColor Yellow