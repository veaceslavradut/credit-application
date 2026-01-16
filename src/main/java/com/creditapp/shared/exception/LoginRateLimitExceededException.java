package com.creditapp.shared.exception;

public class LoginRateLimitExceededException extends RuntimeException {
    public LoginRateLimitExceededException(String message) { super(message); }
    public LoginRateLimitExceededException(String message, Throwable cause) { super(message, cause); }
}