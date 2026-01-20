package com.creditapp.shared.security;

/**
 * Exception thrown when decryption operations fail.
 */
public class DecryptionException extends RuntimeException {
    
    public DecryptionException(String message) {
        super(message);
    }
    
    public DecryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}