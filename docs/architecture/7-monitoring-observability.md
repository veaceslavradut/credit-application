# 7. MONITORING & OBSERVABILITY

## 7.1 Metrics (Prometheus)

**Key Metrics:**
- **Application Performance:** Request latency (p50, p95, p99), request count, error rate
- **Business Metrics:** Applications submitted, offers generated, time-to-offer distribution
- **Infrastructure:** CPU/memory/disk usage, database connection pool, Redis cache hit rate

**Alerts:**
- API error rate >5% → Page on-call
- Uptime <99% (rolling 1 hour) → Warning
- Offer calculation >600ms → Investigation
- Database connection pool >90% → Scale

## 7.2 Logging (ELK Stack)

**Log Collection:**
- All application logs to stdout/stderr
- Fluentd/Logstash ships to Elasticsearch
- Kibana for visualization

**Log Retention:**
- 30 days for debug/info logs
- 90 days for warn/error logs
- 7 years for audit logs (separate index)

---
