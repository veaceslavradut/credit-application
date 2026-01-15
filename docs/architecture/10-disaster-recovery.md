# 10. DISASTER RECOVERY

## 10.1 Backup & Recovery

**Database Backups:**
- Automated daily snapshots to S3 (retain 30 days)
- Point-in-time recovery enabled (retain 7 days of WAL logs)
- Monthly full backup to cold storage (Glacier)

**Recovery Time Objective (RTO):** <1 hour  
**Recovery Point Objective (RPO):** <15 minutes

## 10.2 High Availability

**Multi-AZ Deployment:**
- Primary database in us-east-1a
- Standby in us-east-1b (synchronous replication)
- Automatic failover (RDS Failover) <2 minutes

**No Single Point of Failure:**
- API Gateway: 2+ replicas
- Each microservice: 3+ replicas
- Database: Multi-AZ active-passive
- Cache (Redis): Cluster mode with sharding

---
