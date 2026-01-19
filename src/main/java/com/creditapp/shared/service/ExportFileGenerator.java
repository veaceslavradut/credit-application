package com.creditapp.shared.service;

import com.creditapp.auth.repository.UserRepository;
import com.creditapp.bank.model.Offer;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.borrower.model.Application;
import com.creditapp.borrower.model.ApplicationHistory;
import com.creditapp.borrower.repository.ApplicationHistoryRepository;
import com.creditapp.borrower.repository.ApplicationRepository;
import com.creditapp.shared.model.AuditLog;
import com.creditapp.shared.model.ExportFormat;
import com.creditapp.shared.model.GDPRConsent;
import com.creditapp.shared.model.User;
import com.creditapp.shared.repository.AuditLogRepository;
import com.creditapp.shared.repository.GDPRConsentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportFileGenerator {
    
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationHistoryRepository applicationHistoryRepository;
    private final OfferRepository offerRepository;
    private final GDPRConsentRepository gdprConsentRepository;
    private final AuditLogRepository auditLogRepository;
    
    @Transactional(readOnly = true)
    public JsonNode generateJsonExport(UUID borrowerId) {
        log.info("Generating JSON export for borrower: {}", borrowerId);
        
        ObjectNode root = objectMapper.createObjectNode();
        
        root.put("borrowerId", borrowerId.toString());
        root.put("exportedAt", LocalDateTime.now().toString());
        root.put("dataFormat", "GDPR Article 20 - Structured Machine-Readable Format");
        
        // Profile section
        root.set("profile", buildProfileSection(borrowerId));
        
        // Applications section
        root.set("applications", buildApplicationsSection(borrowerId));
        
        // Offers section
        root.set("offers", buildOffersSection(borrowerId));
        
        // Consents section
        root.set("consents", buildConsentsSection(borrowerId));
        
        // Audit log section
        root.set("auditLog", buildAuditLogSection(borrowerId));
        
        log.info("JSON export generated successfully for borrower: {}", borrowerId);
        return root;
    }
    
    private JsonNode buildProfileSection(UUID borrowerId) {
        ObjectNode profile = objectMapper.createObjectNode();
        
        Optional<User> userOpt = userRepository.findById(borrowerId);
        if (userOpt.isEmpty()) {
            profile.put("error", "User not found");
            return profile;
        }
        
        User user = userOpt.get();
        profile.put("id", user.getId().toString());
        profile.put("email", user.getEmail());
        profile.put("firstName", user.getFirstName());
        profile.put("lastName", user.getLastName());
        profile.put("phoneNumber", user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
        profile.put("role", user.getRole() != null ? user.getRole().toString() : "");
        profile.put("active", user.isActive());
        profile.put("accountCreatedAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : "");
        
        return profile;
    }
    
    private JsonNode buildApplicationsSection(UUID borrowerId) {
        ArrayNode applications = objectMapper.createArrayNode();
        
        List<Application> apps = applicationRepository.findByBorrowerIdOrderByCreatedAtDesc(borrowerId, PageRequest.of(0, 1000));
        
        for (Application app : apps) {
            ObjectNode appNode = objectMapper.createObjectNode();
            appNode.put("id", app.getId().toString());
            appNode.put("loanType", app.getLoanType() != null ? app.getLoanType().toString() : "");
            appNode.put("amount", app.getAmount() != null ? app.getAmount().toString() : "0");
            appNode.put("currency", app.getCurrency() != null ? app.getCurrency().toString() : "");
            appNode.put("status", app.getStatus() != null ? app.getStatus().toString() : "");
            appNode.put("submittedAt", app.getSubmittedAt() != null ? app.getSubmittedAt().toString() : "");
            appNode.put("createdAt", app.getCreatedAt() != null ? app.getCreatedAt().toString() : "");
            appNode.put("updatedAt", app.getUpdatedAt() != null ? app.getUpdatedAt().toString() : "");
            
            // Application history
            List<ApplicationHistory> history = applicationHistoryRepository.findByApplicationIdOrderByCreatedAtDesc(app.getId());
            if (!history.isEmpty()) {
                ArrayNode historyArray = objectMapper.createArrayNode();
                for (ApplicationHistory h : history) {
                    ObjectNode histNode = objectMapper.createObjectNode();
                    histNode.put("status", h.getStatus() != null ? h.getStatus().toString() : "");
                    histNode.put("changedAt", h.getCreatedAt() != null ? h.getCreatedAt().toString() : "");
                    histNode.put("notes", h.getNotes() != null ? h.getNotes() : "");
                    historyArray.add(histNode);
                }
                appNode.set("history", historyArray);
            }
            
            applications.add(appNode);
        }
        
        return applications;
    }
    
    private JsonNode buildOffersSection(UUID borrowerId) {
        ArrayNode offers = objectMapper.createArrayNode();
        
        List<Application> apps = applicationRepository.findByBorrowerIdOrderByCreatedAtDesc(borrowerId, PageRequest.of(0, 1000));
        
        for (Application app : apps) {
            List<Offer> appOffers = offerRepository.findByApplicationId(app.getId());
            
            for (Offer offer : appOffers) {
                ObjectNode offerNode = objectMapper.createObjectNode();
                offerNode.put("id", offer.getId().toString());
                offerNode.put("applicationId", app.getId().toString());
                offerNode.put("bankId", offer.getBankId() != null ? offer.getBankId().toString() : "");
                offerNode.put("apr", offer.getApr() != null ? offer.getApr().toString() : "0");
                offerNode.put("monthlyPayment", offer.getMonthlyPayment() != null ? offer.getMonthlyPayment().toString() : "0");
                offerNode.put("termMonths", offer.getTermMonths() != null ? offer.getTermMonths().toString() : "0");
                offerNode.put("status", offer.getStatus() != null ? offer.getStatus().toString() : "");
                offerNode.put("expiresAt", offer.getExpiresAt() != null ? offer.getExpiresAt().toString() : "");
                offerNode.put("createdAt", offer.getCreatedAt() != null ? offer.getCreatedAt().toString() : "");
                offers.add(offerNode);
            }
        }
        
        return offers;
    }
    
    private JsonNode buildConsentsSection(UUID borrowerId) {
        ArrayNode consents = objectMapper.createArrayNode();
        
        List<GDPRConsent> userConsents = gdprConsentRepository.findByUserIdOrderByConsentedAtDesc(borrowerId);
        
        for (GDPRConsent consent : userConsents) {
            ObjectNode consentNode = objectMapper.createObjectNode();
            consentNode.put("id", consent.getId().toString());
            consentNode.put("consentType", consent.getConsentType() != null ? consent.getConsentType().toString() : "");
            consentNode.put("consentedAt", consent.getConsentedAt() != null ? consent.getConsentedAt().toString() : "");
            consentNode.put("withdrawnAt", consent.getWithdrawnAt() != null ? consent.getWithdrawnAt().toString() : "");
            consentNode.put("ipAddress", consent.getIpAddress() != null ? consent.getIpAddress() : "");
            consentNode.put("userAgent", consent.getUserAgent() != null ? consent.getUserAgent() : "");
            consents.add(consentNode);
        }
        
        return consents;
    }
    
    private JsonNode buildAuditLogSection(UUID borrowerId) {
        ArrayNode auditLogs = objectMapper.createArrayNode();
        
        List<AuditLog> logs = auditLogRepository.findByActorIdOrderByTimestampDesc(borrowerId, PageRequest.of(0, 500));
        
        for (AuditLog log : logs) {
            ObjectNode logNode = objectMapper.createObjectNode();
            logNode.put("id", log.getId() != null ? log.getId().toString() : "");
            logNode.put("action", log.getAction() != null ? log.getAction().toString() : "");
            logNode.put("entityType", log.getEntityType() != null ? log.getEntityType() : "");
            logNode.put("entityId", log.getEntityId() != null ? log.getEntityId().toString() : "");
            logNode.put("timestamp", log.getTimestamp() != null ? log.getTimestamp().toString() : "");
            logNode.put("ipAddress", log.getIpAddress() != null ? log.getIpAddress() : "");
            logNode.put("success", log.isSuccess());
            auditLogs.add(logNode);
        }
        
        return auditLogs;
    }
    
    public byte[] generatePdfExport(UUID borrowerId) {
        log.info("Generating PDF export for borrower: {}", borrowerId);
        
        // TODO: Implement PDF generation with iText library
        // For now, return placeholder
        String placeholder = "PDF Export for Borrower: " + borrowerId + "\\n\\nExported at: " + LocalDateTime.now();
        return placeholder.getBytes();
    }
}