package com.creditapp.bank.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity for storing detailed calculation logs for all offer calculations.
 * This provides an immutable audit trail of all calculations performed.
 */
@Entity
@Table(name = "offer_calculation_log")
public class OfferCalculationLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "application_id", nullable = false)
    private UUID applicationId;
    
    @Column(name = "bank_id")
    private UUID bankId;
    
    @Column(name = "calculation_method")
    private String calculationMethod;
    
    @Type(JsonType.class)
    @Column(name = "input_parameters", columnDefinition = "JSONB")
    private String inputParameters;
    
    @Type(JsonType.class)
    @Column(name = "calculated_values", columnDefinition = "JSONB")
    private String calculatedValues;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_type", nullable = false)
    private CalculationType calculationType;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public OfferCalculationLog() {}

    public OfferCalculationLog(UUID applicationId, UUID bankId, String calculationMethod,
                              String inputParameters, String calculatedValues,
                              CalculationType calculationType, LocalDateTime timestamp) {
        this.applicationId = applicationId;
        this.bankId = bankId;
        this.calculationMethod = calculationMethod;
        this.inputParameters = inputParameters;
        this.calculatedValues = calculatedValues;
        this.calculationType = calculationType;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public String getCalculationMethod() {
        return calculationMethod;
    }

    public void setCalculationMethod(String calculationMethod) {
        this.calculationMethod = calculationMethod;
    }

    public String getInputParameters() {
        return inputParameters;
    }

    public void setInputParameters(String inputParameters) {
        this.inputParameters = inputParameters;
    }

    public String getCalculatedValues() {
        return calculatedValues;
    }

    public void setCalculatedValues(String calculatedValues) {
        this.calculatedValues = calculatedValues;
    }

    public CalculationType getCalculationType() {
        return calculationType;
    }

    public void setCalculationType(CalculationType calculationType) {
        this.calculationType = calculationType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}