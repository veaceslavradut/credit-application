package com.creditapp.shared.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

@Slf4j
@Service
public class RequestContextService {

    public String getCurrentIpAddress() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request == null) return null;

            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }

            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp;
            }

            return request.getRemoteAddr();
        } catch (Exception e) {
            log.warn("Failed to get current IP address", e);
            return null;
        }
    }

    public String getCurrentUserAgent() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request == null) return null;
            return request.getHeader("User-Agent");
        } catch (Exception e) {
            log.warn("Failed to get current User-Agent", e);
            return null;
        }
    }

    public UUID getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }
            Object principal = authentication.getPrincipal();
            if (principal instanceof String) {
                return UUID.fromString((String) principal);
            }
            return null;
        } catch (Exception e) {
            log.warn("Failed to get current user ID", e);
            return null;
        }
    }

    public String getCurrentUserRole() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }
            return authentication.getAuthorities().stream()
                    .findFirst()
                    .map(auth -> auth.getAuthority())
                    .orElse(null);
        } catch (Exception e) {
            log.warn("Failed to get current user role", e);
            return null;
        }
    }

    public HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) return null;
            return attributes.getRequest();
        } catch (Exception e) {
            log.warn("Failed to get current HTTP request", e);
            return null;
        }
    }
}