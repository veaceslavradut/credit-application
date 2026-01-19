package com.creditapp.shared.service;

import com.creditapp.shared.model.DataExport;
import com.sendgrid.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataExportEmailService {
    
    private final EmailService emailService;
    
    @Value("${app.base-url:https://creditapp.com}")
    private String baseUrl;
    
    @Value("${app.download-endpoint:/api/borrower/data-export/download}")
    private String downloadEndpoint;

    public void sendDataExportReadyEmail(DataExport export, String borrowerEmail) {
        try {
            log.info("Sending data export ready email to: {}", borrowerEmail);
            
            // Load HTML template
            String template = loadEmailTemplate();
            
            // Build download link with token
            String downloadLink = buildDownloadLink(export.getDownloadToken());
            
            // Format file size (placeholder - would get actual size from S3)
            String fileSize = "~1-50 MB";
            
            // Format export timestamp
            String generatedAt = formatDateTime(export.getCompletedAt() != null ? export.getCompletedAt() : LocalDateTime.now());
            
            // Replace placeholders
            String htmlContent = template
                .replace("{DOWNLOAD_LINK}", downloadLink)
                .replace("{FILE_FORMAT}", export.getFormat().name())
                .replace("{FILE_SIZE}", fileSize)
                .replace("{GENERATED_AT}", generatedAt);
            
            // Create plain text version
            String textContent = createPlainTextVersion(borrowerEmail, export, downloadLink);
            
            // Send email
            Response response = emailService.sendEmail(
                borrowerEmail,
                "Your Data Export is Ready",
                htmlContent,
                textContent
            );
            
            if (response != null && response.getStatusCode() == 202) {
                log.info("Data export email sent successfully to: {}", borrowerEmail);
            } else {
                log.warn("Data export email send returned status: {}", 
                    response != null ? response.getStatusCode() : "null");
            }
            
        } catch (Exception e) {
            log.error("Error sending data export ready email to: {}", borrowerEmail, e);
            // Don't throw - allow export to complete even if email fails
        }
    }
    
    private String loadEmailTemplate() {
        try {
            String templatePath = "src/main/resources/templates/data-export-ready.html";
            return new String(Files.readAllBytes(Paths.get(templatePath)));
        } catch (IOException e) {
            log.error("Error loading email template", e);
            // Return fallback template if file not found
            return getFallbackTemplate();
        }
    }
    
    private String buildDownloadLink(String downloadToken) {
        return String.format("%s%s?token=%s", baseUrl, downloadEndpoint, downloadToken);
    }
    
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Unknown";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm:ss");
        return dateTime.format(formatter);
    }
    
    private String createPlainTextVersion(String email, DataExport export, String downloadLink) {
        return String.format(
            "Your Data Export is Ready\n\n" +
            "Hello,\n\n" +
            "Your data export request has been processed successfully. Your personal data file is ready for download.\n\n" +
            "Download Your Data:\n" +
            "Click this link to download your data export: %s\n\n" +
            "File Information:\n" +
            "- Format: %s\n" +
            "- Generated at: %s\n\n" +
            "What's Included:\n" +
            "- Profile: Your personal information\n" +
            "- Applications: All loan applications with history\n" +
            "- Offers: All offers received for your applications\n" +
            "- Consents: Record of all data processing consents\n" +
            "- Audit Log: Recent activity log\n\n" +
            "Important Information:\n" +
            "- Link Expiry: This download link will expire in 24 hours.\n" +
            "- One-Time Use: This link can only be used once for security reasons.\n" +
            "- Sensitive Data: Please store this file securely and delete after verification.\n\n" +
            "Need Help?\n" +
            "If you have questions, contact support at support@creditapp.com\n\n" +
            "Best regards,\nCredit Application Platform Team\n\n" +
            "This is an automated email. Please do not reply.",
            downloadLink,
            export.getFormat().name()
        );
    }
    
    private String getFallbackTemplate() {
        return "<html><body>" +
            "<h1>Your Data Export is Ready</h1>" +
            "<p>Download your data: {DOWNLOAD_LINK}</p>" +
            "<p>Format: {FILE_FORMAT}</p>" +
            "<p>This link expires in 24 hours.</p>" +
            "</body></html>";
    }
}