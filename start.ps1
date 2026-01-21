# Credit Application Platform - Quick Start Script
# This script starts all services using Docker Compose

Write-Host "============================================" -ForegroundColor Cyan
Write-Host " Credit Application Platform - Quick Start" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Check if Docker is running
Write-Host "Checking Docker..." -ForegroundColor Yellow
try {
    docker version | Out-Null
    Write-Host " Docker is running" -ForegroundColor Green
} catch {
    Write-Host " Docker is not running. Please start Docker Desktop first." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Starting services..." -ForegroundColor Yellow
Write-Host ""

# Stop and remove existing containers
docker compose down 2>&1 | Out-Null

# Start services
docker compose up -d

Write-Host ""
Write-Host "Waiting for services to be healthy..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

# Check service status
Write-Host ""
Write-Host "Service Status:" -ForegroundColor Cyan
docker compose ps

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host " Services are starting up!" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Access Points:" -ForegroundColor Yellow
Write-Host "   Backend API:     http://localhost:8080/api" -ForegroundColor White
Write-Host "   Backend Health:  http://localhost:8080/actuator/health" -ForegroundColor White
Write-Host "   Database UI:     http://localhost:8081" -ForegroundColor White
Write-Host "   PostgreSQL:      localhost:5432" -ForegroundColor White
Write-Host "   Redis:           localhost:6379" -ForegroundColor White
Write-Host ""
Write-Host "Database Credentials:" -ForegroundColor Yellow
Write-Host "   System:   PostgreSQL" -ForegroundColor White
Write-Host "   Server:   postgres" -ForegroundColor White
Write-Host "   Username: creditapp" -ForegroundColor White
Write-Host "   Password: creditapp_dev_password" -ForegroundColor White
Write-Host "   Database: credit_app" -ForegroundColor White
Write-Host ""
Write-Host "Useful Commands:" -ForegroundColor Yellow
Write-Host "   View logs:       docker compose logs -f backend" -ForegroundColor White
Write-Host "   Stop services:   docker compose down" -ForegroundColor White
Write-Host "   Restart backend: docker compose restart backend" -ForegroundColor White
Write-Host ""
Write-Host "Note: Backend may take 30-60 seconds to fully start" -ForegroundColor Gray
Write-Host "      Check health: http://localhost:8080/actuator/health" -ForegroundColor Gray
Write-Host ""
