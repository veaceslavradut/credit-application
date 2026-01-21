# Credit Application Platform - Stop Script
# This script stops all services

Write-Host "============================================" -ForegroundColor Cyan
Write-Host " Credit Application Platform - Stop" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Stopping services..." -ForegroundColor Yellow
docker compose down

Write-Host ""
Write-Host " All services stopped" -ForegroundColor Green
Write-Host ""
Write-Host "To remove all data (volumes), run:" -ForegroundColor Yellow
Write-Host "  docker compose down -v" -ForegroundColor White
Write-Host ""
