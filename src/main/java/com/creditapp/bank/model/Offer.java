package com.creditapp.bank.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a loan offer made by a bank for a borrower's application.
 */
@Entity
@Table(name = "offers")
public class Offer {
    
    @Id
    private UUID id;
    
    @Column(name = "application_id", nullable = false)
    private UUID applicationId;
    
    @Column(name = "bank_id", nullable = false)
    private UUID bankId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "offer_status", nullable = false)
    private OfferStatus offerStatus;
    
    @Column(name = "apr", nullable = false)
    private BigDecimal apr;
    
    @Column(name = "monthly_payment", nullable = false)
    private BigDecimal monthlyPayment;
    
    @Column(name = "total_cost", nullable = false)
    private BigDecimal totalCost;
    
    @Column(name = "origination_fee", nullable = false)
    private BigDecimal originationFee;
    
    @Column(name = "insurance_cost")
    private BigDecimal insuranceCost;
    
    @Column(name = "processing_time_days", nullable = false)
    private Integer processingTimeDays;
    
    @Column(name = "validity_period_days", nullable = false)
    private Integer validityPeriodDays;
    
    @Column(name = "required_documents")
    private String requiredDocuments;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "offer_submitted_at")
    private LocalDateTime offerSubmittedAt;
    
    @Column(name = "borrower_selected_at")
    private LocalDateTime borrowerSelectedAt;
    
    @Column(name = "submitted_by_officer_id")
    private UUID submittedByOfficerId;
    
    @Column(name = "submission_notes", length = 2000)
    private String submissionNotes;
    
    @Column(name = "office_notes", length = 2000)
    private String officeNotes;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "notified", nullable = false)
    private boolean notified = false;

    // Constructors
    public Offer() {}

    public Offer(UUID id, UUID applicationId, UUID bankId, OfferStatus offerStatus,
                 BigDecimal apr, BigDecimal monthlyPayment, BigDecimal totalCost,
                 BigDecimal originationFee, BigDecimal insuranceCost, Integer processingTimeDays,
                 Integer validityPeriodDays, String requiredDocuments, LocalDateTime expiresAt) {
        this.id = id;
        this.applicationId = applicationId;
        this.bankId = bankId;
        this.offerStatus = offerStatus;
        this.apr = apr;
        this.monthlyPayment = monthlyPayment;
        this.totalCost = totalCost;
        this.originationFee = originationFee;
        this.insuranceCost = insuranceCost;
        this.processingTimeDays = processingTimeDays;
        this.validityPeriodDays = validityPeriodDays;
        this.requiredDocuments = requiredDocuments;
        this.expiresAt = expiresAt;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    public UUID getBankId() {
        return bankId;
    }

    public void setBankId(UUID bankId) {
        this.bankId = bankId;
    }

    public OfferStatus getOfferStatus() {
        return offerStatus;
    }

    public void setOfferStatus(OfferStatus offerStatus) {
        this.offerStatus = offerStatus;
    }

    public BigDecimal getApr() {
        return apr;
    }

    public void setApr(BigDecimal apr) {
        this.apr = apr;
    }

    public BigDecimal getMonthlyPayment() {
        return monthlyPayment;
    }

    public void setMonthlyPayment(BigDecimal monthlyPayment) {
        this.monthlyPayment = monthlyPayment;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public BigDecimal getOriginationFee() {
        return originationFee;
    }

    public void setOriginationFee(BigDecimal originationFee) {
        this.originationFee = originationFee;
    }

    public BigDecimal getInsuranceCost() {
        return insuranceCost;
    }

    public void setInsuranceCost(BigDecimal insuranceCost) {
        this.insuranceCost = insuranceCost;
    }

    public Integer getProcessingTimeDays() {
        return processingTimeDays;
    }

    public void setProcessingTimeDays(Integer processingTimeDays) {
        this.processingTimeDays = processingTimeDays;
    }

    public Integer getValidityPeriodDays() {
        return validityPeriodDays;
    }

    public void setValidityPeriodDays(Integer validityPeriodDays) {
        this.validityPeriodDays = validityPeriodDays;
    }

    public String getRequiredDocuments() {
        return requiredDocuments;
    }

    public void setRequiredDocuments(String requiredDocuments) {
        this.requiredDocuments = requiredDocuments;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getOfferSubmittedAt() {
        return offerSubmittedAt;
    }

    public void setOfferSubmittedAt(LocalDateTime offerSubmittedAt) {
        this.offerSubmittedAt = offerSubmittedAt;
    }

    public LocalDateTime getBorrowerSelectedAt() {
        return borrowerSelectedAt;
    }

    public void setBorrowerSelectedAt(LocalDateTime borrowerSelectedAt) {
        this.borrowerSelectedAt = borrowerSelectedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public UUID getSubmittedByOfficerId() {
        return submittedByOfficerId;
    }

    public void setSubmittedByOfficerId(UUID submittedByOfficerId) {
        this.submittedByOfficerId = submittedByOfficerId;
    }

    public String getSubmissionNotes() {
        return submissionNotes;
    }

    public void setSubmissionNotes(String submissionNotes) {
        this.submissionNotes = submissionNotes;
    }

    public String getOfficeNotes() {
        return officeNotes;
    }

    public void setOfficeNotes(String officeNotes) {
        this.officeNotes = officeNotes;
    }

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    /**
     * Check if this offer has expired.
     * @return true if expiresAt is before now, false otherwise
     */
    @Transient
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}