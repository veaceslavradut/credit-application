package com.creditapp.shared.service;

import java.util.UUID;

public interface EmailService {
    void sendRegistrationConfirmation(String email, String userName, UUID userId);
}