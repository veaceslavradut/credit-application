package com.creditapp.bank.exception;

/**
 * Exception thrown when offer calculation fails.
 */
public class OfferCalculationException extends RuntimeException {
    public OfferCalculationException(String message) {
        super(message);
    }
    
    public OfferCalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}