package com.creditapp.borrower.exception;

/**
 * Exception thrown when attempting to withdraw an application in terminal state.
 */
public class ApplicationNotWithdrawableException extends RuntimeException {
    private String currentStatus;

    public ApplicationNotWithdrawableException(String message) {
        super(message);
    }

    public ApplicationNotWithdrawableException(String message, String currentStatus) {
        super(message);
        this.currentStatus = currentStatus;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }
}
