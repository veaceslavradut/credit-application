package com.creditapp.bank.exception;

public class DuplicateRateCardException extends RuntimeException {
    
    public DuplicateRateCardException(String message) {
        super(message);
    }
    
    public DuplicateRateCardException(String message, Throwable cause) {
        super(message, cause);
    }
}