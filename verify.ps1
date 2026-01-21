# Credit Application Platform - Setup Verification Script
# This script verifies that all services are running correctly

Write-Host "============================================" -ForegroundColor Cyan
Write-Host " Setup Verification" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

$allGood = $true

# Check Docker
Write-Host "Checking Docker..." -ForegroundColor Yellow
try {
    docker version | Out-Null
    Write-Host " Docker is running" -ForegroundColor Green
} catch {
    Write-Host " Docker is not running" -ForegroundColor Red
    $allGood = $false
}

Write-Host ""
Write-Host "Checking Services..." -ForegroundColor Yellow
Write-Host ""

# Get service status
$services = docker compose ps --format json 2>&1 | ConvertFrom-Json

if (-not $services) {
    Write-Host " No services are running" -ForegroundColor Red
    Write-Host "  Run: docker compose up -d" -ForegroundColor Yellow
    exit 1
}

# Ensure $services is an array
if ($services -isnot [System.Array]) {
    $services = @($services)
}

# Check each service
$serviceNames = @("postgres", "redis", "backend", "adminer")
foreach ($serviceName in $serviceNames) {
    $service = $services | Where-Object { $_.Service -eq $serviceName }
    
    if (-not $service) {
        Write-Host " $serviceName - Not found" -ForegroundColor Red
        $allGood = $false
    } elseif ($service.State -ne "running") {
        Write-Host " $serviceName - $($service.State)" -ForegroundColor Red
        $allGood = $false
    } else {
        # Check health status
        if ($service.Health -and $service.Health -ne "healthy") {
            Write-Host " $serviceName - Running but not healthy yet ($($service.Health))" -ForegroundColor Yellow
        } else {
            Write-Host " $serviceName - Running" -ForegroundColor Green
        }
    }
}

Write-Host ""
Write-Host "Testing Endpoints..." -ForegroundColor Yellow
Write-Host ""

# Test PostgreSQL
Write-Host "Testing PostgreSQL connection..." -ForegroundColor Gray
try {
    $pgTest = docker exec credit-app-postgres psql -U creditapp -d credit_app -c "SELECT version();" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host " PostgreSQL - Accepting connections" -ForegroundColor Green
    } else {
        Write-Host " PostgreSQL - Connection failed" -ForegroundColor Red
        $allGood = $false
    }
} catch {
    Write-Host " PostgreSQL - Cannot connect" -ForegroundColor Red
    $allGood = $false
}

# Test Redis
Write-Host "Testing Redis connection..." -ForegroundColor Gray
try {
    $redisTest = docker exec credit-app-redis redis-cli -a creditapp_redis_password PING 2>&1
    if ($redisTest -match "PONG") {
        Write-Host " Redis - Accepting connections" -ForegroundColor Green
    } else {
        Write-Host " Redis - Connection failed" -ForegroundColor Red
        $allGood = $false
    }
} catch {
    Write-Host " Redis - Cannot connect" -ForegroundColor Red
    $allGood = $false
}

# Test Backend Health (with retry)
Write-Host "Testing Backend health endpoint..." -ForegroundColor Gray
$maxRetries = 3
$retryCount = 0
$backendHealthy = $false

while ($retryCount -lt $maxRetries -and -not $backendHealthy) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing -TimeoutSec 5 2>&1
        if ($response.StatusCode -eq 200) {
            $backendHealthy = $true
            Write-Host " Backend - Health check passed" -ForegroundColor Green
        }
    } catch {
        $retryCount++
        if ($retryCount -lt $maxRetries) {
            Write-Host "  Retry $retryCount/$maxRetries..." -ForegroundColor Gray
            Start-Sleep -Seconds 2
        }
    }
}

if (-not $backendHealthy) {
    Write-Host " Backend - Health check failed (may still be starting up)" -ForegroundColor Red
    Write-Host "  Check logs: docker compose logs -f backend" -ForegroundColor Yellow
    $allGood = $false
}

# Test Adminer
Write-Host "Testing Adminer..." -ForegroundColor Gray
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081" -UseBasicParsing -TimeoutSec 5 2>&1
    if ($response.StatusCode -eq 200) {
        Write-Host " Adminer - Accessible" -ForegroundColor Green
    }
} catch {
    Write-Host " Adminer - Not accessible" -ForegroundColor Red
    $allGood = $false
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan

if ($allGood) {
    Write-Host " All Systems Operational! " -ForegroundColor Green
    Write-Host "============================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Access Points:" -ForegroundColor Yellow
    Write-Host "   Backend API:     http://localhost:8080/api" -ForegroundColor White
    Write-Host "   Backend Health:  http://localhost:8080/actuator/health" -ForegroundColor White
    Write-Host "   Database UI:     http://localhost:8081" -ForegroundColor White
    Write-Host ""
    Write-Host "Next Steps:" -ForegroundColor Yellow
    Write-Host "   Test API: curl http://localhost:8080/actuator/health" -ForegroundColor White
    Write-Host "   View logs: docker compose logs -f backend" -ForegroundColor White
    Write-Host "   Create backup: .\backup.ps1" -ForegroundColor White
    Write-Host ""
} else {
    Write-Host " Some Issues Detected" -ForegroundColor Red
    Write-Host "============================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Troubleshooting:" -ForegroundColor Yellow
    Write-Host "   View logs: docker compose logs -f" -ForegroundColor White
    Write-Host "   Restart: docker compose restart" -ForegroundColor White
    Write-Host "   Check status: docker compose ps" -ForegroundColor White
    Write-Host "   Full reset: docker compose down -v && docker compose up -d" -ForegroundColor White
    Write-Host ""
}
