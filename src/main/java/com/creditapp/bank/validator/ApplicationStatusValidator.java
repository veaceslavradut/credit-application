package com.creditapp.bank.validator;

import com.creditapp.borrower.model.ApplicationStatus;

public class ApplicationStatusValidator {

    public static boolean isValidTransition(ApplicationStatus from, ApplicationStatus to) {
        if (from == null || to == null) {
            return false;
        }
        if (from.equals(to)) {
            return false;
        }

        return switch (from) {
            case OFFERS_AVAILABLE -> to == ApplicationStatus.ACCEPTED || to == ApplicationStatus.REJECTED;
            case ACCEPTED -> to == ApplicationStatus.COMPLETED || to == ApplicationStatus.REJECTED;
            case UNDER_REVIEW -> to == ApplicationStatus.OFFERS_AVAILABLE || to == ApplicationStatus.REJECTED;
            case SUBMITTED -> to == ApplicationStatus.UNDER_REVIEW || to == ApplicationStatus.REJECTED;
            default -> false;
        };
    }

    public static String getTransitionErrorMessage(ApplicationStatus from, ApplicationStatus to) {
        return String.format("Invalid status transition from %s to %s", from, to);
    }
}