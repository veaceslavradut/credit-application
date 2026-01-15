# 9. COST OPTIMIZATION

**Infrastructure Costs (Monthly Estimate for MVP):**

| Component | Usage | Cost |
|-----------|-------|------|
| **EKS Cluster** | 2 nodes (on-demand) | $150 |
| **RDS PostgreSQL** | db.t3.small (multi-AZ) | $250 |
| **ElastiCache Redis** | cache.t3.small (cluster) | $100 |
| **S3 Storage** | 50 GB | $10 |
| **Data Transfer** | 500 GB/month | $50 |
| **CloudFront CDN** | Static assets | $20 |
| **Other** | Route53, backup, monitoring | $70 |
| **TOTAL** | | **~$650/month** |

**Cost Optimization Levers:**
- Reserved Instances (Phase 2): Save 40% on compute
- S3 Lifecycle: Move audit logs to Glacier after 30 days
- Right-sizing: Monitor and adjust instance types quarterly
- Caching: Reduce database query count by 60%

---
