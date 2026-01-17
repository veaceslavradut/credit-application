package com.creditapp.bank.exception;

public class InvalidRateCardException extends RuntimeException {
    
    public InvalidRateCardException(String message) {
        super(message);
    }
    
    public InvalidRateCardException(String message, Throwable cause) {
        super(message, cause);
    }
}