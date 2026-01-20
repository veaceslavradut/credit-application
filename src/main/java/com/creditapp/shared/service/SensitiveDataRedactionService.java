package com.creditapp.shared.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SensitiveDataRedactionService {
    public Map<String, Object> redactSensitiveData(Map<String, Object> details) {
        if (details == null) return null;
        Map<String, Object> redacted = new HashMap<>(details);
        redacted.replaceAll((k, v) -> {
            if (v instanceof String) {
                String s = (String) v;
                if (s.matches("(?i).*[0-9]{3}[- ]?[0-9]{2}[- ]?[0-9]{4}.*")) { // SSN-like
                    return "***REDACTED***";
                }
                if (s.matches("(?i).*\\b[0-9]{10,16}\\b.*")) { // card/phone-like
                    return "***REDACTED***";
                }
                if (s.matches("(?i)^.+@.+$")) { // email
                    String domain = s.substring(s.indexOf('@'));
                    return "***@" + domain.substring(1);
                }
            }
            return v;
        });
        return redacted;
    }
}
