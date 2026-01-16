package com.creditapp.shared.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Service to extract request context information (IP address, user agent)
 */
@Service
public class RequestContextService {

    /**
     * Get the IP address from the current HTTP request
     */
    public String getClientIpAddress() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return "SYSTEM";
        }

        // Check for X-Forwarded-For header (proxy/load balancer)
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress != null && !ipAddress.isEmpty() && !"unknown".equalsIgnoreCase(ipAddress)) {
            // X-Forwarded-For can contain multiple IPs, get the first one
            return ipAddress.split(",")[0].trim();
        }

        // Check for X-Real-IP header
        ipAddress = request.getHeader("X-Real-IP");
        if (ipAddress != null && !ipAddress.isEmpty() && !"unknown".equalsIgnoreCase(ipAddress)) {
            return ipAddress;
        }

        // Fallback to remote address
        ipAddress = request.getRemoteAddr();
        return ipAddress != null ? ipAddress : "UNKNOWN";
    }

    /**
     * Get the user agent from the current HTTP request
     */
    public String getUserAgent() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return "SYSTEM";
        }

        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "UNKNOWN";
    }

    /**
     * Get request context information
     */
    public RequestContext getRequestContext() {
        return new RequestContext(getClientIpAddress(), getUserAgent());
    }

    /**
     * Get the current HTTP request from RequestContextHolder
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Holder for request context data
     */
    @Data
    public static class RequestContext {
        private final String ipAddress;
        private final String userAgent;

        public RequestContext(String ipAddress, String userAgent) {
            this.ipAddress = ipAddress;
            this.userAgent = userAgent;
        }
    }
}
