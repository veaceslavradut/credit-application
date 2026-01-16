package com.creditapp.auth.dto;

import java.util.UUID;

public class RegistrationResponse {
    private UUID userId;
    private String email;
    private String message;

    public RegistrationResponse(UUID userId, String email, String message) {
        this.userId = userId;
        this.email = email;
        this.message = message;
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}