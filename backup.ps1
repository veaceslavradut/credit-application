# Credit Application Platform - Database Backup Script
# This script creates a timestamped backup of the PostgreSQL database

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$backupDir = "backups"
$backupFile = "$backupDir/backup_$timestamp.sql"

Write-Host "============================================" -ForegroundColor Cyan
Write-Host " Database Backup Utility" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Create backups directory if it doesn't exist
if (-not (Test-Path $backupDir)) {
    New-Item -ItemType Directory -Path $backupDir | Out-Null
    Write-Host "Created backups directory" -ForegroundColor Green
}

# Check if database container is running
$containerStatus = docker compose ps postgres --format json 2>&1 | ConvertFrom-Json
if (-not $containerStatus -or $containerStatus.State -ne "running") {
    Write-Host " Database container is not running" -ForegroundColor Red
    Write-Host "  Please start it with: docker compose up -d postgres" -ForegroundColor Yellow
    exit 1
}

Write-Host "Creating backup..." -ForegroundColor Yellow
Write-Host "  Container: credit-app-postgres" -ForegroundColor Gray
Write-Host "  Database:  credit_app" -ForegroundColor Gray
Write-Host "  File:      $backupFile" -ForegroundColor Gray
Write-Host ""

# Create backup
try {
    docker exec credit-app-postgres pg_dump -U creditapp credit_app | `
        Out-File -FilePath $backupFile -Encoding UTF8
    
    $fileSize = (Get-Item $backupFile).Length / 1KB
    Write-Host " Backup created successfully" -ForegroundColor Green
    Write-Host "  Size: $([math]::Round($fileSize, 2)) KB" -ForegroundColor Gray
    Write-Host "  Location: $backupFile" -ForegroundColor Gray
    
    # List recent backups
    Write-Host ""
    Write-Host "Recent backups:" -ForegroundColor Cyan
    Get-ChildItem $backupDir -Filter "backup_*.sql" | `
        Sort-Object LastWriteTime -Descending | `
        Select-Object -First 5 | `
        ForEach-Object {
            $size = [math]::Round($_.Length / 1KB, 2)
            Write-Host "  $($_.Name) - $size KB - $($_.LastWriteTime)" -ForegroundColor Gray
        }
    
} catch {
    Write-Host " Backup failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "To restore this backup:" -ForegroundColor Yellow
Write-Host "  docker exec -i credit-app-postgres psql -U creditapp -d credit_app < $backupFile" -ForegroundColor White
Write-Host ""
