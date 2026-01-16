package com.creditapp.auth.dto;

import java.util.UUID;

public class BankRegistrationResponse {
    private UUID bankId;
    private String bankName;
    private UUID adminUserId;
    private String status;
    private String message;

    public BankRegistrationResponse(UUID bankId, String bankName, UUID adminUserId, String status, String message) {
        this.bankId = bankId;
        this.bankName = bankName;
        this.adminUserId = adminUserId;
        this.status = status;
        this.message = message;
    }

    public UUID getBankId() { return bankId; }
    public void setBankId(UUID bankId) { this.bankId = bankId; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public UUID getAdminUserId() { return adminUserId; }
    public void setAdminUserId(UUID adminUserId) { this.adminUserId = adminUserId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}