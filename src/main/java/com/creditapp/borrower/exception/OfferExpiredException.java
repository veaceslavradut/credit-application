package com.creditapp.borrower.exception;

import java.time.LocalDateTime;

public class OfferExpiredException extends RuntimeException {

    private final LocalDateTime expiresAt;

    public OfferExpiredException(String message, LocalDateTime expiresAt) {
        super(message);
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}