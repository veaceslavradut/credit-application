# Docker Setup Guide

This guide explains how to run the Credit Application Platform using Docker Compose.

## Prerequisites

- Docker Desktop (Windows/Mac) or Docker Engine + Docker Compose (Linux)
- At least 4GB RAM available for Docker
- Ports 5432, 6379, 8080, 8081 available

## Architecture

The application consists of the following services:

```

                     Docker Network                       
  (credit-app-network)                                   
                                                          
                   
  PostgreSQL                                 
    :5432        Backend   Frontend          
        :8080         :3000            
                               
                                           
    Redis                                            
    :6379                                 
                                             
                                                          
                                             
   Adminer                                             
    :8081                                              
                                             

```

## Quick Start

### Start All Services
```powershell
# Using the convenience script (Windows)
.\start.ps1

# Or manually
docker compose up -d
```

### Stop All Services
```powershell
# Using the convenience script (Windows)
.\stop.ps1

# Or manually
docker compose down
```

## Service Details

### PostgreSQL Database (postgres)
- **Image**: postgres:15.4-alpine
- **Port**: 5432
- **Database**: credit_app
- **User**: creditapp
- **Password**: creditapp_dev_password
- **Health Check**: Automatic with 5 retries
- **Data Persistence**: Volume `postgres_data`

### Redis Cache (redis)
- **Image**: redis:7.2.3-alpine
- **Port**: 6379
- **Password**: creditapp_redis_password
- **Data Persistence**: Volume `redis_data` with AOF enabled
- **Health Check**: Automatic ping check

### Spring Boot Backend (backend)
- **Build**: From Dockerfile (multi-stage build)
- **Port**: 8080
- **Dependencies**: postgres, redis (waits for healthy state)
- **Health Check**: Actuator endpoint
- **Startup Time**: 30-60 seconds
- **Volumes**: 
  - `./uploads` - File uploads
  - `./logs` - Application logs

### Adminer (adminer)
- **Image**: adminer:latest
- **Port**: 8081
- **Purpose**: Web-based database management UI
- **Default Server**: postgres

### Frontend (frontend) - Placeholder
- **Status**: Not yet implemented
- **Port**: 3000 (when ready)
- **Technology**: Next.js/React

## Common Operations

### View Logs
```bash
# All services
docker compose logs -f

# Specific service
docker compose logs -f backend
docker compose logs -f postgres

# Last 100 lines
docker compose logs --tail=100 backend
```

### Restart Services
```bash
# Restart all
docker compose restart

# Restart specific service
docker compose restart backend
docker compose restart postgres
```

### Check Service Status
```bash
docker compose ps
```

### Execute Commands in Containers
```bash
# Backend shell
docker compose exec backend bash

# PostgreSQL shell
docker compose exec postgres psql -U creditapp -d credit_app

# Redis CLI
docker compose exec redis redis-cli -a creditapp_redis_password
```

### Database Management

#### Using Adminer (Web UI)
1. Open http://localhost:8081
2. Enter credentials:
   - System: PostgreSQL
   - Server: postgres
   - Username: creditapp
   - Password: creditapp_dev_password
   - Database: credit_app

#### Using Command Line
```bash
# Connect to database
docker exec -it credit-app-postgres psql -U creditapp -d credit_app

# List tables
docker exec -it credit-app-postgres psql -U creditapp -d credit_app -c "\dt"

# Backup database
docker exec credit-app-postgres pg_dump -U creditapp credit_app > backup_$(date +%Y%m%d).sql

# Restore database
docker exec -i credit-app-postgres psql -U creditapp -d credit_app < backup.sql
```

## Development Workflow

### Option 1: Full Docker Stack (Recommended for Testing)
```bash
# Start everything
docker compose up -d

# View backend logs
docker compose logs -f backend

# Make code changes and rebuild
docker compose build backend
docker compose up -d backend
```

### Option 2: Local Development (Faster Iteration)
```bash
# Start only dependencies
docker compose up -d postgres redis adminer

# Run backend locally
mvn spring-boot:run

# Or with IDE (IntelliJ, VS Code)
```

## Environment Configuration

Create a `.env` file from `.env.example`:
```bash
cp .env.example .env
# Edit .env with your custom values
```

The docker-compose.yml will automatically load variables from `.env`.

## Troubleshooting

### Port Already in Use
```bash
# Find what's using the port (Windows)
netstat -ano | findstr "8080"
# Kill the process or change the port in docker-compose.yml
```

### Database Connection Failed
```bash
# Check database is healthy
docker compose ps postgres

# Check logs
docker compose logs postgres

# Restart database
docker compose restart postgres
```

### Backend Not Starting
```bash
# Check backend logs
docker compose logs backend

# Common issues:
# - Database not ready: Wait for postgres health check
# - Port conflict: Change port mapping
# - Build failed: Check Maven dependencies

# Rebuild from scratch
docker compose down
docker compose build --no-cache backend
docker compose up -d
```

### Clean Start (Reset Everything)
```bash
# Stop and remove containers + volumes
docker compose down -v

# Remove images (optional)
docker compose down --rmi all

# Start fresh
docker compose up -d
```

### Performance Issues
```bash
# Check resource usage
docker stats

# Increase Docker Desktop memory:
# Settings > Resources > Memory > 4GB+

# Prune unused resources
docker system prune -a
```

## Production Deployment

### Security Checklist
- [ ] Change all default passwords
- [ ] Use strong JWT secret (32+ characters)
- [ ] Enable SSL/TLS for all connections
- [ ] Set `SPRING_PROFILES_ACTIVE=prod`
- [ ] Configure proper CORS origins
- [ ] Review and restrict network access
- [ ] Set up proper backup strategy
- [ ] Configure log rotation
- [ ] Enable monitoring and alerts

### Production Configuration
```yaml
# Example production overrides in docker-compose.prod.yml
services:
  postgres:
    environment:
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}  # From secrets
  
  backend:
    environment:
      SPRING_PROFILES_ACTIVE: prod
      JWT_SECRET: ${JWT_SECRET}  # From secrets
      LOGGING_LEVEL_ROOT: WARN
```

## Monitoring

### Health Checks
```bash
# Backend health
curl http://localhost:8080/actuator/health

# Database health
docker compose ps postgres

# Redis health
docker exec credit-app-redis redis-cli -a creditapp_redis_password PING
```

### Metrics
```bash
# Backend metrics
curl http://localhost:8080/actuator/metrics

# Docker stats
docker stats
```

## Backup and Recovery

### Automated Backup Script (Windows)
```powershell
# backup.ps1
$date = Get-Date -Format "yyyyMMdd_HHmmss"
docker exec credit-app-postgres pg_dump -U creditapp credit_app | `
  Out-File -FilePath "backup_$date.sql" -Encoding UTF8
Write-Host "Backup created: backup_$date.sql"
```

### Recovery
```bash
# Stop backend to avoid conflicts
docker compose stop backend

# Restore database
docker exec -i credit-app-postgres psql -U creditapp -d credit_app < backup.sql

# Start backend
docker compose start backend
```

## Additional Resources

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [PostgreSQL Docker Hub](https://hub.docker.com/_/postgres)
- [Redis Docker Hub](https://hub.docker.com/_/redis)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)

## Support

For issues specific to this project, check:
1. Service logs: `docker compose logs -f <service>`
2. Health checks: `docker compose ps`
3. Resource usage: `docker stats`
