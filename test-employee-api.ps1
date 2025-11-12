# Test Employee Management API
$baseUrl = "http://localhost:8081/api/device"
$deviceKey = "020e7096a03c670f63"
$secret = "123456"

Write-Host "🔍 Testing Employee Management API..." -ForegroundColor Cyan
Write-Host "Device: $deviceKey" -ForegroundColor Yellow
Write-Host ""

# Test 1: List existing employees on device
Write-Host "1. 📋 Listing current employees on device..." -ForegroundColor Green
$listResponse = Invoke-RestMethod -Uri "$baseUrl/list" -Method Post -ContentType "application/json" -Body (@{
    deviceKey = $deviceKey
    secret = $secret
} | ConvertTo-Json)
Write-Host "Response:" -ForegroundColor Yellow
$listResponse | ConvertTo-Json -Depth 5

Write-Host "`n" + "="*50 + "`n"

# Test 2: Sync a new employee
Write-Host "2. ➕ Syncing new employee..." -ForegroundColor Green
$syncData = @{
    employeeId = "EMP001"
    fullName = "John Doe"
    faceImage = ""  # Empty for now - would contain base64 image
    deviceKey = $deviceKey
    secret = $secret
    department = "IT"
    position = "Developer"
}
try {
    $syncResponse = Invoke-RestMethod -Uri "$baseUrl/sync" -Method Post -ContentType "application/json" -Body ($syncData | ConvertTo-Json)
    Write-Host "Sync Response:" -ForegroundColor Yellow
    $syncResponse | ConvertTo-Json -Depth 5
} catch {
    Write-Host "Sync Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorBody = $reader.ReadToEnd()
        Write-Host "Error Details: $errorBody" -ForegroundColor Red
    }
}

Write-Host "`n" + "="*50 + "`n"

# Test 3: Remove an employee (if we have one to remove)
Write-Host "3. ➖ Testing employee removal..." -ForegroundColor Green
$removeData = @{
    employeeId = "EMP001"  # Try to remove the one we just added
    deviceKey = $deviceKey
    secret = $secret
}
try {
    $removeResponse = Invoke-RestMethod -Uri "$baseUrl/remove" -Method Post -ContentType "application/json" -Body ($removeData | ConvertTo-Json)
    Write-Host "Remove Response:" -ForegroundColor Yellow
    $removeResponse | ConvertTo-Json -Depth 5
} catch {
    Write-Host "Remove Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorBody = $reader.ReadToEnd()
        Write-Host "Error Details: $errorBody" -ForegroundColor Red
    }
}

Write-Host "`n" + "="*50 + "`n"
Write-Host "✅ Employee Management API Test Complete!" -ForegroundColor Green