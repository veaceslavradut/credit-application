package com.creditapp.shared.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Service for redacting PII from audit logs and other sensitive contexts.
 * Ensures GDPR compliance by preventing PII leakage in logs.
 */
@Service
@Slf4j
public class DataRedactionService {

    private static final String REDACTED = "[REDACTED]";
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@]+@(.+)$");

    /**
     * Redacts email address, keeping only domain.
     * Example: john.doe@example.com -> ***@example.com
     */
    public String redactEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }

        var matcher = EMAIL_PATTERN.matcher(email);
        if (matcher.matches()) {
            return "***@" + matcher.group(1);
        }
        return REDACTED;
    }

    /**
     * Redacts phone number completely.
     */
    public String redactPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return phone;
        }
        return REDACTED;
    }

    /**
     * Redacts name, keeping only first letter.
     * Example: John Doe -> J*** D***
     */
    public String redactName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        String[] parts = name.split("\\s+");
        StringBuilder redacted = new StringBuilder();
        
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                redacted.append(" ");
            }
            if (parts[i].length() > 0) {
                redacted.append(parts[i].charAt(0)).append("***");
            }
        }
        
        return redacted.toString();
    }

    /**
     * Redacts address completely.
     */
    public String redactAddress(String address) {
        if (address == null || address.isEmpty()) {
            return address;
        }
        return REDACTED;
    }

    /**
     * Redacts sensitive fields from audit log details map.
     * Removes: email (redacted), phone, name, address, password, passwordHash, ssn
     */
    public Map<String, Object> redactAuditDetails(Map<String, Object> details) {
        if (details == null) {
            return null;
        }

        Map<String, Object> redacted = new HashMap<>(details);

        // Redact email (keep domain)
        if (redacted.containsKey("email")) {
            redacted.put("email", redactEmail(String.valueOf(redacted.get("email"))));
        }

        // Remove sensitive fields completely
        redacted.remove("phone");
        redacted.remove("phoneNumber");
        redacted.remove("name");
        redacted.remove("firstName");
        redacted.remove("lastName");
        redacted.remove("address");
        redacted.remove("password");
        redacted.remove("passwordHash");
        redacted.remove("ssn");
        redacted.remove("nationalId");
        redacted.remove("taxId");

        return redacted;
    }

    /**
     * Redacts sensitive query parameters from URLs.
     */
    public String redactUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }

        // Remove common sensitive query parameters
        return url
            .replaceAll("([?&])password=[^&]*", "$1password=" + REDACTED)
            .replaceAll("([?&])token=[^&]*", "$1token=" + REDACTED)
            .replaceAll("([?&])secret=[^&]*", "$1secret=" + REDACTED)
            .replaceAll("([?&])key=[^&]*", "$1key=" + REDACTED);
    }
}