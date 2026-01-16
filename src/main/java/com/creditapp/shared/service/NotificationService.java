package com.creditapp.shared.service;

import com.creditapp.shared.dto.EmailMetricsDTO;
import com.creditapp.shared.model.DeliveryStatus;
import com.creditapp.shared.model.EmailDeliveryLog;
import com.creditapp.shared.model.EmailTemplate;
import com.creditapp.shared.repository.EmailDeliveryLogRepository;
import com.creditapp.shared.repository.EmailTemplateRepository;
import com.creditapp.shared.util.EmailRateLimiter;
import com.sendgrid.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * NotificationService orchestrates email notifications
 * Handles template processing, variable substitution, and delivery tracking
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final EmailTemplateRepository emailTemplateRepository;
    private final EmailDeliveryLogRepository emailDeliveryLogRepository;
    private final EmailService emailService;
    private final RabbitTemplate rabbitTemplate;
    private final EmailRateLimiter emailRateLimiter;
    
    /**
     * Send email using template with variable substitution
     * @param recipient recipient email address
     * @param templateName template name
     * @param variables template variables
     * @return true if sent successfully
     */
    @Transactional
    public boolean sendEmail(String recipient, String templateName, Map<String, String> variables) {
        try {
            // Check rate limit before sending
            if (!emailRateLimiter.checkRateLimit()) {
                log.warn("Rate limit exceeded for email to: {}", recipient);
                logDeliveryStatus(recipient, templateName, DeliveryStatus.FAILED, 
                    "Rate limit exceeded - email will be retried");
                return false;
            }
            
            // Fetch template
            EmailTemplate template = emailTemplateRepository
                .findByTemplateNameAndActiveTrue(templateName)
                .orElseThrow(() -> new RuntimeException("Email template not found: " + templateName));
            
            // Substitute variables in template
            String subject = substituteVariables(template.getSubject(), variables);
            String htmlBody = substituteVariables(template.getHtmlBody(), variables);
            String textBody = substituteVariables(template.getTextBody(), variables);
            
            // Send email via email service
            Response response = emailService.sendEmail(recipient, subject, htmlBody, textBody);
            
            // Log delivery
            DeliveryStatus status = (response.getStatusCode() >= 200 && response.getStatusCode() < 300) 
                ? DeliveryStatus.SENT 
                : DeliveryStatus.FAILED;
            
            logDeliveryStatus(recipient, templateName, status, null);
            
            return status == DeliveryStatus.SENT;
            
        } catch (Exception e) {
            log.error("Failed to send email to {} using template {}: {}", 
                recipient, templateName, e.getMessage());
            logDeliveryStatus(recipient, templateName, DeliveryStatus.FAILED, e.getMessage());
            return false;
        }
    }
    
    /**
     * Substitute variables in template text
     * Replaces {variableName} placeholders with actual values
     */
    private String substituteVariables(String template, Map<String, String> variables) {
        if (template == null || variables == null) {
            return template;
        }
        
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            result = result.replace(placeholder, entry.getValue() != null ? entry.getValue() : "");
        }
        return result;
    }
    
    /**
     * Queue notification for async processing
     * @param eventType event type (e.g., APPLICATION_SUBMITTED)
     * @param recipient recipient email
     * @param variables template variables
     */
    public void queueNotification(String eventType, String recipient, Map<String, String> variables) {
        try {
            com.creditapp.shared.dto.NotificationEvent event = com.creditapp.shared.dto.NotificationEvent.builder()
                .id(java.util.UUID.randomUUID())
                .eventType(eventType)
                .recipientEmail(recipient)
                .templateName(eventType)
                .variables(variables)
                .timestamp(LocalDateTime.now())
                .retryCount(0)
                .build();
            
            rabbitTemplate.convertAndSend(
                com.creditapp.shared.config.RabbitMQConfig.NOTIFICATION_EXCHANGE,
                "notification.event",
                event
            );
            
            log.info("Queued notification event: {} for recipient: {}", eventType, recipient);
            
        } catch (Exception e) {
            log.error("Failed to queue notification: {}", e.getMessage());
        }
    }
    
    /**
     * Log email delivery status
     */
    public void logDeliveryStatus(String recipientEmail, String templateName, 
                                    DeliveryStatus status, String errorMessage) {
        EmailDeliveryLog log = EmailDeliveryLog.builder()
            .recipientEmail(recipientEmail)
            .templateName(templateName)
            .status(status)
            .sentAt(LocalDateTime.now())
            .errorMessage(errorMessage)
            .build();
        
        emailDeliveryLogRepository.save(log);
    }
    
    /**
     * Get email metrics for health check
     */
    public EmailMetricsDTO getEmailMetrics() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        
        Long sent = emailDeliveryLogRepository.countByStatusAndSentAtAfter(
            DeliveryStatus.SENT, oneHourAgo);
        Long delivered = emailDeliveryLogRepository.countByStatusAndSentAtAfter(
            DeliveryStatus.DELIVERED, oneHourAgo);
        Long bounced = emailDeliveryLogRepository.countByStatusAndSentAtAfter(
            DeliveryStatus.BOUNCED, oneHourAgo);
        Long failed = emailDeliveryLogRepository.countByStatusAndSentAtAfter(
            DeliveryStatus.FAILED, oneHourAgo);
        
        long total = sent + delivered + bounced + failed;
        double successRate = total > 0 ? (double) (sent + delivered) / total : 0.0;
        double failureRate = total > 0 ? (double) (bounced + failed) / total : 0.0;
        
        return EmailMetricsDTO.builder()
            .emailsSent(sent)
            .emailsDelivered(delivered)
            .emailsBounced(bounced)
            .emailsFailed(failed)
            .successRate(successRate)
            .failureRate(failureRate)
            .build();
    }
}
