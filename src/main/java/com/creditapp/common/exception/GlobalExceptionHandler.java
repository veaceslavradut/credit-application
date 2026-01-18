package com.creditapp.common.exception;

import com.creditapp.auth.exception.DuplicateEmailException;
import com.creditapp.auth.exception.DuplicateBankRegistrationException;
import com.creditapp.auth.exception.PasswordValidationException;
import com.creditapp.auth.exception.InvalidCredentialsException;
import com.creditapp.auth.exception.BankNotActivatedException;
import com.creditapp.auth.service.InvalidPasswordException;
import com.creditapp.bank.exception.OfferCalculationException;
import com.creditapp.bank.exception.RateCardNotFoundException;
import com.creditapp.bank.exception.InvalidRateCardException;
import com.creditapp.bank.exception.DuplicateRateCardException;
import com.creditapp.borrower.exception.ApplicationAlreadySubmittedException;
import com.creditapp.borrower.exception.ApplicationCreationException;
import com.creditapp.borrower.exception.ApplicationLockedException;
import com.creditapp.borrower.exception.ApplicationNotEditableException;
import com.creditapp.borrower.exception.ApplicationNotWithdrawableException;
import com.creditapp.borrower.exception.ApplicationStaleException;
import com.creditapp.borrower.exception.DocumentNotFoundException;
import com.creditapp.borrower.exception.DocumentStorageException;
import com.creditapp.borrower.exception.FileSizeExceededException;
import com.creditapp.borrower.exception.InvalidApplicationException;
import com.creditapp.borrower.exception.InvalidDocumentException;
import com.creditapp.borrower.exception.InvalidOfferException;
import com.creditapp.borrower.exception.NotificationNotFoundException;
import com.creditapp.borrower.exception.OfferExpiredException;
import com.creditapp.borrower.exception.SubmissionValidationException;
import com.creditapp.shared.exception.LoginRateLimitExceededException;
import com.creditapp.shared.exception.NotFoundException;
import com.creditapp.shared.exception.RateLimitExceededException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<?> handleDuplicateEmail(DuplicateEmailException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Conflict");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(DuplicateBankRegistrationException.class)
    public ResponseEntity<?> handleDuplicateBankRegistration(DuplicateBankRegistrationException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Conflict");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(PasswordValidationException.class)
    public ResponseEntity<?> handlePasswordValidation(PasswordValidationException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Bad Request");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<?> handleInvalidCredentials(InvalidCredentialsException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Unauthorized");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(BankNotActivatedException.class)
    public ResponseEntity<?> handleBankNotActivated(BankNotActivatedException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Unauthorized");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(LoginRateLimitExceededException.class)
    public ResponseEntity<?> handleLoginRateLimitExceeded(LoginRateLimitExceededException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Too Many Requests");
        response.put("message", ex.getMessage());
        response.put("retryAfter", 60);
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<?> handleInvalidPassword(InvalidPasswordException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Unauthorized");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFound(NotFoundException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Not Found");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();
        String firstErrorMessage = "Validation failed";
        
        var errors = ex.getBindingResult().getAllErrors();
        if (!errors.isEmpty()) {
            FieldError firstFieldError = (FieldError) errors.get(0);
            firstErrorMessage = firstFieldError.getDefaultMessage();
        }
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        response.put("error", "Bad Request");
        response.put("message", firstErrorMessage);
        response.put("fieldErrors", fieldErrors);
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Bad Request");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(InvalidApplicationException.class)
    public ResponseEntity<?> handleInvalidApplication(InvalidApplicationException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Invalid Application");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ApplicationCreationException.class)
    public ResponseEntity<?> handleApplicationCreation(ApplicationCreationException ex, WebRequest request) {
        logger.error("Application creation failed", ex);
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Internal Server Error");
        response.put("message", "Failed to create application. Please try again later.");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(ApplicationNotEditableException.class)
    public ResponseEntity<?> handleApplicationNotEditable(ApplicationNotEditableException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Conflict");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ApplicationStaleException.class)
    public ResponseEntity<?> handleApplicationStale(ApplicationStaleException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Stale Update");
        response.put("message", ex.getMessage());
        response.put("currentVersion", ex.getCurrentVersion());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ApplicationAlreadySubmittedException.class)
    public ResponseEntity<?> handleApplicationAlreadySubmitted(ApplicationAlreadySubmittedException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Application Already Submitted");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ApplicationNotWithdrawableException.class)
    public ResponseEntity<?> handleApplicationNotWithdrawable(ApplicationNotWithdrawableException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Cannot Withdraw");
        response.put("message", ex.getMessage());
        response.put("currentStatus", ex.getCurrentStatus());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(SubmissionValidationException.class)
    public ResponseEntity<?> handleSubmissionValidation(SubmissionValidationException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Submission Validation Failed");
        response.put("message", ex.getMessage());
        response.put("missingFields", ex.getMissingFields());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<?> handleRateLimitExceeded(RateLimitExceededException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Rate Limit Exceeded");
        response.put("message", ex.getMessage());
        response.put("retryAfter", ex.getRetryAfterSeconds());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    @ExceptionHandler(InvalidDocumentException.class)
    public ResponseEntity<?> handleInvalidDocument(InvalidDocumentException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Invalid Document");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    @ExceptionHandler(RateCardNotFoundException.class)
    public ResponseEntity<?> handleRateCardNotFound(RateCardNotFoundException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Not Found");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InvalidRateCardException.class)
    public ResponseEntity<?> handleInvalidRateCard(InvalidRateCardException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Bad Request");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DuplicateRateCardException.class)
    public ResponseEntity<?> handleDuplicateRateCard(DuplicateRateCardException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Conflict");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    @ExceptionHandler(FileSizeExceededException.class)
    public ResponseEntity<?> handleFileSizeExceeded(FileSizeExceededException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Payload Too Large");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }

    @ExceptionHandler(ApplicationLockedException.class)
    public ResponseEntity<?> handleApplicationLocked(ApplicationLockedException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Application Locked");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(DocumentStorageException.class)
    public ResponseEntity<?> handleDocumentStorage(DocumentStorageException ex, WebRequest request) {
        logger.error("Document storage error", ex);
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Storage Error");
        response.put("message", "Failed to store document. Please try again.");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<?> handleDocumentNotFound(DocumentNotFoundException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Not Found");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<?> handleNotificationNotFound(NotificationNotFoundException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Not Found");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(com.creditapp.borrower.exception.ApplicationNotFoundException.class)
    public ResponseEntity<?> handleApplicationNotFound(com.creditapp.borrower.exception.ApplicationNotFoundException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Not Found");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(OfferCalculationException.class)
    public ResponseEntity<?> handleOfferCalculationException(OfferCalculationException ex, WebRequest request) {
        logger.error("Offer calculation error", ex);
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Internal Server Error");
        response.put("message", "Offer calculation failed. Please try again later.");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // DO NOT handle AccessDeniedException here - let Spring Security's CustomAccessDeniedHandler handle it
    // @ExceptionHandler(AccessDeniedException.class) is intentionally omitted

    @ExceptionHandler(OfferExpiredException.class)
    public ResponseEntity<?> handleOfferExpired(OfferExpiredException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Offer Expired");
        response.put("message", ex.getMessage());
        response.put("expiresAt", ex.getExpiresAt());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.GONE).body(response);
    }

    @ExceptionHandler(InvalidOfferException.class)
    public ResponseEntity<?> handleInvalidOffer(InvalidOfferException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Invalid Offer");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        
        // Check message to determine appropriate status code
        String message = ex.getMessage();
        if (message != null) {
            if (message.contains("Application not found")) {
                response.put("error", "Not Found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            } else if (message.contains("Unauthorized access")) {
                response.put("error", "Forbidden");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFound(EntityNotFoundException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Not Found");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex, WebRequest request) {
        // Rethrow AccessDeniedException so Spring Security can handle it properly
        if (ex instanceof AccessDeniedException) {
            throw (AccessDeniedException) ex;
        }
        
        logger.error("Unexpected error", ex);
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Internal Server Error");
        response.put("message", "An unexpected error occurred");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}