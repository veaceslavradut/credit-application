package com.creditapp.shared.config;

import com.sendgrid.SendGrid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SendGrid email service configuration
 * Creates SendGrid client bean when sendgrid.enabled=true
 */
@Configuration
@Slf4j
public class SendGridConfig {
    
    @Value("${sendgrid.api-key}")
    private String apiKey;
    
    @Value("${sendgrid.from-email}")
    private String fromEmail;
    
    @Value("${sendgrid.enabled:true}")
    private boolean enabled;
    
    /**
     * Create SendGrid client bean
     * Only created when sendgrid.enabled=true
     */
    @Bean
    @ConditionalOnProperty(name = "sendgrid.enabled", havingValue = "true", matchIfMissing = true)
    public SendGrid sendGrid() {
        if (apiKey == null || apiKey.equals("test-key")) {
            log.warn("SendGrid API key is not configured. Email sending will fail in production.");
        }
        log.info("SendGrid client initialized with from-email: {}", fromEmail);
        return new SendGrid(apiKey);
    }
    
    @Bean
    public String sendGridFromEmail() {
        return fromEmail;
    }
}