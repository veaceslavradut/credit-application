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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ExportFileGenerator {
    
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationHistoryRepository applicationHistoryRepository;
    private final OfferRepository offerRepository;
    private final GDPRConsentRepository gdprConsentRepository;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public JsonNode generateJsonExport(UUID borrowerId) {
        log.info("Generating JSON export for borrower: {}", borrowerId);
        
        ObjectNode export = objectMapper.createObjectNode();
        export.put("borrowerId", borrowerId.toString());
        export.put("exportedAt", LocalDateTime.now().toString());
        export.put("dataFormat", "JSON");
        
        export.set("profile", buildProfileSection(borrowerId));
        export.set("applications", buildApplicationsSection(borrowerId));
        export.set("offers", buildOffersSection(borrowerId));
        export.set("consents", buildConsentsSection(borrowerId));
        export.set("auditLog", buildAuditLogSection(borrowerId));
        
        return export;
    }
    
    private JsonNode buildProfileSection(UUID borrowerId) {
        return userRepository.findById(borrowerId)
            .map(user -> {
                ObjectNode profile = objectMapper.createObjectNode();
                profile.put("id", user.getId().toString());
                profile.put("email", user.getEmail() != null ? user.getEmail() : "");
                profile.put("firstName", user.getFirstName() != null ? user.getFirstName() : "");
                profile.put("lastName", user.getLastName() != null ? user.getLastName() : "");
                profile.put("phoneNumber", user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
                profile.put("role", user.getRole() != null ? user.getRole().name() : "");
                profile.put("active", user.getIsActive() != null ? user.getIsActive() : false);
                profile.put("accountCreatedAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : "");
                return (JsonNode) profile;
            })
            .orElse(objectMapper.createObjectNode().put("error", "User not found"));
    }
    
    private JsonNode buildApplicationsSection(UUID borrowerId) {
        ArrayNode applications = objectMapper.createArrayNode();
        
        List<Application> apps = applicationRepository.findByBorrowerId(borrowerId, PageRequest.of(0, 1000)).getContent();
        
        for (Application app : apps) {
            ObjectNode appNode = objectMapper.createObjectNode();
            appNode.put("id", app.getId().toString());
            appNode.put("loanType", app.getLoanType() != null ? app.getLoanType() : "");
            appNode.put("loanAmount", app.getLoanAmount() != null ? app.getLoanAmount().toString() : "0");
            appNode.put("loanTermMonths", app.getLoanTermMonths() != null ? app.getLoanTermMonths() : 0);
            appNode.put("currency", app.getCurrency() != null ? app.getCurrency() : "");
            appNode.put("status", app.getStatus() != null ? app.getStatus().name() : "");
            appNode.put("createdAt", app.getCreatedAt() != null ? app.getCreatedAt().toString() : "");
            appNode.put("submittedAt", app.getSubmittedAt() != null ? app.getSubmittedAt().toString() : "");
            
            ArrayNode history = objectMapper.createArrayNode();
            List<ApplicationHistory> historyRecords = applicationHistoryRepository.findByApplicationIdOrderByChangedAtDesc(app.getId());
            for (ApplicationHistory h : historyRecords) {
                ObjectNode histNode = objectMapper.createObjectNode();
                histNode.put("oldStatus", h.getOldStatus() != null ? h.getOldStatus().name() : "");
                histNode.put("newStatus", h.getNewStatus() != null ? h.getNewStatus().name() : "");
                histNode.put("changedAt", h.getChangedAt() != null ? h.getChangedAt().toString() : "");
                histNode.put("changeReason", h.getChangeReason() != null ? h.getChangeReason() : "");
                history.add(histNode);
            }
            appNode.set("history", history);
            applications.add(appNode);
        }
        
        return applications;
    }
    
    private JsonNode buildOffersSection(UUID borrowerId) {
        ArrayNode offers = objectMapper.createArrayNode();
        
        List<Application> apps = applicationRepository.findByBorrowerId(borrowerId, PageRequest.of(0, 1000)).getContent();
        
        for (Application app : apps) {
            List<Offer> appOffers = offerRepository.findByApplicationId(app.getId());
            for (Offer offer : appOffers) {
                ObjectNode offerNode = objectMapper.createObjectNode();
                offerNode.put("id", offer.getId().toString());
                offerNode.put("applicationId", offer.getApplicationId().toString());
                offerNode.put("bankId", offer.getBankId().toString());
                offerNode.put("apr", offer.getApr() != null ? offer.getApr().toString() : "0");
                offerNode.put("monthlyPayment", offer.getMonthlyPayment() != null ? offer.getMonthlyPayment().toString() : "0");
                offerNode.put("totalCost", offer.getTotalCost() != null ? offer.getTotalCost().toString() : "0");
                offerNode.put("processingTimeDays", offer.getProcessingTimeDays() != null ? offer.getProcessingTimeDays() : 0);
                offerNode.put("status", offer.getOfferStatus() != null ? offer.getOfferStatus().name() : "");
                offerNode.put("expiresAt", offer.getExpiresAt() != null ? offer.getExpiresAt().toString() : "");
                offers.add(offerNode);
            }
        }
        
        return offers;
    }
    
    private JsonNode buildConsentsSection(UUID borrowerId) {
        ArrayNode consents = objectMapper.createArrayNode();
        
        List<GDPRConsent> userConsents = gdprConsentRepository.findAllByBorrowerId(borrowerId);
        
        for (GDPRConsent consent : userConsents) {
            ObjectNode consentNode = objectMapper.createObjectNode();
            consentNode.put("id", consent.getId().toString());
            consentNode.put("consentType", consent.getConsentType() != null ? consent.getConsentType().name() : "");
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
        
        List<AuditLog> logs = auditLogRepository.findByActorIdOrderByCreatedAtDesc(borrowerId);
        if (logs.size() > 500) {
            logs = logs.subList(0, 500);
        }
        
        for (AuditLog log : logs) {
            ObjectNode logNode = objectMapper.createObjectNode();
            logNode.put("id", log.getId() != null ? log.getId().toString() : "");
            logNode.put("action", log.getAction() != null ? log.getAction().name() : "");
            logNode.put("entityType", log.getEntityType() != null ? log.getEntityType() : "");
            logNode.put("entityId", log.getEntityId() != null ? log.getEntityId().toString() : "");
            logNode.put("timestamp", log.getCreatedAt() != null ? log.getCreatedAt().toString() : "");
            logNode.put("ipAddress", log.getIpAddress() != null ? log.getIpAddress() : "");
            auditLogs.add(logNode);
        }
        
        return auditLogs;
    }
    
    public byte[] generatePdfExport(UUID borrowerId, ExportFormat format) {
        log.warn("PDF export not yet implemented for borrower: {}", borrowerId);
        return "PDF export not yet implemented".getBytes();
    }
}