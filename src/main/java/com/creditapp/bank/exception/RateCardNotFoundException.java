package com.creditapp.bank.exception;

public class RateCardNotFoundException extends RuntimeException {
    
    public RateCardNotFoundException(String message) {
        super(message);
    }
    
    public RateCardNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}