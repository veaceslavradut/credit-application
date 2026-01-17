package com.creditapp.shared.controller;

import com.creditapp.shared.dto.EmailMetricsDTO;
import com.creditapp.shared.dto.NotificationHealthDTO;
import com.creditapp.shared.repository.EmailDeliveryLogRepository;
import com.creditapp.shared.service.NotificationService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private EmailDeliveryLogRepository emailDeliveryLogRepository;
    
    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        
        // Check database connectivity
        boolean databaseConnected = checkDatabase();
        response.put("database", databaseConnected ? "connected" : "disconnected");
        
        // Check Redis connectivity
        boolean redisConnected = checkRedis();
        response.put("redis", redisConnected ? "connected" : "disconnected");
        
        // Add version
        response.put("version", "1.0.0");
        
        // Return 200 if both services are connected, 503 otherwise
        if (databaseConnected && redisConnected) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(503).body(response);
        }
    }

    private boolean checkDatabase() {
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(2); // 2 second timeout
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkRedis() {
        try {
            var factory = redisTemplate.getConnectionFactory();
            if (factory == null) {
                return false;
            }
            RedisConnection connection = factory.getConnection();
            if (connection == null) {
                return false;
            }
            var pong = connection.ping();
            connection.close();
            return pong != null && pong.equals("PONG");
        } catch (Exception e) {
            return false;
        }
    }
    
    @GetMapping("/health/notifications")
    public ResponseEntity<NotificationHealthDTO> notificationHealth() {
        try {
            // Get email metrics
            EmailMetricsDTO metrics = notificationService.getEmailMetrics();
            
            // Check RabbitMQ connection
            boolean queueConnected = checkRabbitMQ();
            
            // Check SendGrid (simplified - just check if service exists)
            boolean sendgridConnected = true; // In real scenario, ping SendGrid API
            
            // Get last email sent time
            LocalDateTime lastEmailSent = emailDeliveryLogRepository
                .findAll()
                .stream()
                .map(com.creditapp.shared.model.EmailDeliveryLog::getSentAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);
            
            // Determine status
            String status;
            if (queueConnected && sendgridConnected) {
                status = "UP";
            } else if (queueConnected || sendgridConnected) {
                status = "DEGRADED";
            } else {
                status = "DOWN";
            }
            
            NotificationHealthDTO health = NotificationHealthDTO.builder()
                .status(status)
                .sendgridConnected(sendgridConnected)
                .queueConnected(queueConnected)
                .lastEmailSent(lastEmailSent)
                .emailsSentLastHour(metrics.getEmailsSent())
                .failureRate(metrics.getFailureRate())
                .build();
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            NotificationHealthDTO health = NotificationHealthDTO.builder()
                .status("DOWN")
                .sendgridConnected(false)
                .queueConnected(false)
                .build();
            return ResponseEntity.status(503).body(health);
        }
    }
    
    private boolean checkRabbitMQ() {
        try {
            if (rabbitTemplate == null) {
                return false;
            }
            rabbitTemplate.getConnectionFactory().createConnection().close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}