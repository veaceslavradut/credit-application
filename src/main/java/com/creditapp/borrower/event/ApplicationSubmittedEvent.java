package com.creditapp.borrower.event;

import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ApplicationSubmittedEvent extends ApplicationEvent {
    private final UUID applicationId;
    private final UUID borrowerId;
    private final String loanType;
    private final BigDecimal loanAmount;
    private final Integer loanTermMonths;
    private final String currency;
    private final LocalDateTime submittedAt;
    
    public ApplicationSubmittedEvent(Object source, UUID applicationId, UUID borrowerId,
                                     String loanType, BigDecimal loanAmount, 
                                     Integer loanTermMonths, String currency,
                                     LocalDateTime submittedAt) {
        super(source);
        this.applicationId = applicationId;
        this.borrowerId = borrowerId;
        this.loanType = loanType;
        this.loanAmount = loanAmount;
        this.loanTermMonths = loanTermMonths;
        this.currency = currency;
        this.submittedAt = submittedAt;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public UUID getBorrowerId() {
        return borrowerId;
    }

    public String getLoanType() {
        return loanType;
    }

    public BigDecimal getLoanAmount() {
        return loanAmount;
    }

    public Integer getLoanTermMonths() {
        return loanTermMonths;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
}