# Quick Reference Card - Credit Application Platform

##  Quick Start

```powershell
# Start everything
.\start.ps1
# or
docker compose up -d

# Verify setup
.\verify.ps1

# Stop everything
.\stop.ps1
# or
docker compose down
```

##  Access Points

| Service | URL | Credentials |
|---------|-----|-------------|
| Backend API | http://localhost:8080/api | - |
| Health Check | http://localhost:8080/actuator/health | - |
| Adminer (DB UI) | http://localhost:8081 | Server: postgres<br>User: creditapp<br>Pass: creditapp_dev_password<br>DB: credit_app |
| PostgreSQL | localhost:5432 | User: creditapp<br>Pass: creditapp_dev_password |
| Redis | localhost:6379 | Pass: creditapp_redis_password |

##  Common Commands

### Service Management
```powershell
# Start all services
docker compose up -d

# Start specific services
docker compose up -d postgres redis

# Stop all services
docker compose down

# Stop and remove volumes (DANGER: deletes data)
docker compose down -v

# Restart a service
docker compose restart backend

# View service status
docker compose ps
```

### Logs
```powershell
# View all logs
docker compose logs -f

# View backend logs
docker compose logs -f backend

# View last 100 lines
docker compose logs --tail=100 backend

# View logs for all services
docker compose logs postgres redis backend
```

### Database Operations
```powershell
# Create backup
.\backup.ps1

# Connect to database
docker exec -it credit-app-postgres psql -U creditapp -d credit_app

# List tables
docker exec -it credit-app-postgres psql -U creditapp -d credit_app -c "\dt"

# Backup manually
docker exec credit-app-postgres pg_dump -U creditapp credit_app > backup.sql

# Restore backup
docker exec -i credit-app-postgres psql -U creditapp -d credit_app < backup.sql
```

### Redis Operations
```powershell
# Connect to Redis CLI
docker exec -it credit-app-redis redis-cli -a creditapp_redis_password

# Ping Redis
docker exec credit-app-redis redis-cli -a creditapp_redis_password PING

# View all keys
docker exec credit-app-redis redis-cli -a creditapp_redis_password KEYS "*"

# Clear cache
docker exec credit-app-redis redis-cli -a creditapp_redis_password FLUSHALL
```

### Development
```powershell
# Start dependencies only (for local dev)
docker compose up -d postgres redis adminer
mvn spring-boot:run

# Rebuild backend
docker compose build backend
docker compose up -d backend

# Build and run locally
mvn clean package
java -jar target/credit-application-*.jar

# Run tests
mvn test

# Run with profile
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

##  Troubleshooting

### Port Conflicts
```powershell
# Check what's using a port (Windows)
netstat -ano | findstr "8080"
# Kill process or change port in docker-compose.yml
```

### Database Issues
```powershell
# Check database health
docker compose ps postgres
docker compose logs postgres

# Reset database
docker compose down -v
docker compose up -d
```

### Backend Issues
```powershell
# View detailed logs
docker compose logs -f backend

# Rebuild from scratch
docker compose down
docker compose build --no-cache backend
docker compose up -d backend

# Check health
curl http://localhost:8080/actuator/health
```

### Performance
```powershell
# Check resource usage
docker stats

# Prune unused resources
docker system prune -a

# Increase Docker memory: Settings > Resources > Memory > 4GB+
```

### Clean Slate
```powershell
# Complete reset
docker compose down -v
docker compose build --no-cache
docker compose up -d
```

##  Project Structure

```
credit-application/
 docker-compose.yml     # Service definitions
 Dockerfile            # Backend image build
 .env.example          # Environment template
 start.ps1            # Quick start script
 stop.ps1             # Quick stop script
 backup.ps1           # Database backup script
 verify.ps1           # Setup verification script
 DOCKER_SETUP.md      # Detailed documentation
 README.md            # Project documentation
 src/                 # Source code
 target/              # Build output
 uploads/             # File uploads (volume)
 logs/                # Application logs (volume)
 backups/            # Database backups
```

##  Security Notes

### Development (Current)
- Default passwords (clearly marked)
- No SSL/TLS (localhost only)
- Permissive CORS

### Production (Required)
- [ ] Change all passwords
- [ ] Use secrets management
- [ ] Enable SSL/TLS
- [ ] Restrict CORS
- [ ] Set `SPRING_PROFILES_ACTIVE=prod`
- [ ] Configure monitoring
- [ ] Implement automated backups

##  Documentation

- **Quick Start**: README.md
- **Docker Setup**: DOCKER_SETUP.md
- **Architecture**: docs/architecture.md
- **API Docs**: docs/API_ENDPOINTS.md
- **Database Schema**: docs/DATABASE_SCHEMA.md

##  Support

If something isn't working:
1. Run `.\verify.ps1` to check service status
2. Check logs: `docker compose logs -f backend`
3. Review DOCKER_SETUP.md troubleshooting section
4. Try clean restart: `docker compose down -v && docker compose up -d`

##  Pro Tips

1. **Use verify.ps1 regularly** to check all services
2. **Create backups before major changes** using backup.ps1
3. **Use Adminer** for visual database exploration
4. **Monitor logs** during development: `docker compose logs -f backend`
5. **Keep volumes** when stopping (use `docker compose down` not `down -v`)
6. **Use local dev workflow** for faster iteration (dependencies in Docker, backend local)

---

**Remember**: Backend takes 30-60 seconds to fully start. Check http://localhost:8080/actuator/health
