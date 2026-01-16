package com.creditapp.auth.exception;

public class BankNotActivatedException extends RuntimeException {
    public BankNotActivatedException(String message) { super(message); }
    public BankNotActivatedException(String message, Throwable cause) { super(message, cause); }
}