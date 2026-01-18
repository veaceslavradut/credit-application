package com.creditapp.shared.validator;

import lombok.Getter;

import java.math.BigDecimal;

public class APRValidator {

    public static final BigDecimal MIN_APR = BigDecimal.valueOf(4.0);
    public static final BigDecimal MAX_APR = BigDecimal.valueOf(20.0);

    public static ValidationResult validateAPR(BigDecimal apr) {
        if (apr == null) {
            return ValidationResult.invalid("APR cannot be null");
        }

        if (apr.compareTo(MIN_APR) < 0) {
            return ValidationResult.invalid(
                    String.format("APR %.2f%% is below minimum of %.2f%%", apr, MIN_APR)
            );
        }

        if (apr.compareTo(MAX_APR) > 0) {
            return ValidationResult.invalid(
                    String.format("APR %.2f%% exceeds maximum of %.2f%%", apr, MAX_APR)
            );
        }

        return ValidationResult.valid();
    }

    public static APRComparison compareWithSystemOffer(BigDecimal submittedAPR, BigDecimal systemAPR) {
        if (submittedAPR == null || systemAPR == null) {
            return APRComparison.UNKNOWN;
        }

        int comparison = submittedAPR.compareTo(systemAPR);

        if (comparison < 0) {
            return APRComparison.BETTER_TERMS; // Lower APR is better
        } else if (comparison == 0) {
            return APRComparison.SAME;
        } else {
            return APRComparison.WORSE_TERMS; // Higher APR is worse
        }
    }

    @Getter
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }
    }

    public enum APRComparison {
        BETTER_TERMS,  // Submitted APR < System APR (better for borrower)
        SAME,          // Submitted APR == System APR
        WORSE_TERMS,   // Submitted APR > System APR (worse for borrower)
        UNKNOWN        // One or both APRs are null
    }
}