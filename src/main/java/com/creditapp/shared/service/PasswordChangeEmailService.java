package com.creditapp.shared.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class PasswordChangeEmailService {
    
    @Async
    public void sendPasswordChangeNotification(String email, String userName) {
        try {
            log.info("Password change notification would be sent to: {} (User: {})", email, userName);
            
            String subject = "Your Password Has Been Changed";
            LocalDateTime now = LocalDateTime.now();
            String formattedTime = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String message = String.format(
                    "Hello %s, Your password was changed on %s. If you did not make this change, please reset your password immediately. Best regards, Credit Application Team",
                    userName,
                    formattedTime
            );
            
        } catch (Exception e) {
            log.warn("Failed to send password change notification to {}: {}", email, e.getMessage());
        }
    }
}