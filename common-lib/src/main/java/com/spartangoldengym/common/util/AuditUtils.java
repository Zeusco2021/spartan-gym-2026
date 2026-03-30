package com.spartangoldengym.common.util;

import com.spartangoldengym.common.dto.AuditEntry;

import javax.servlet.http.HttpServletRequest;

public final class AuditUtils {

    private AuditUtils() {
    }

    public static AuditEntry createEntry(String userId, String action, String resourceType, String resourceId, String details, HttpServletRequest request) {
        String ipAddress = extractIpAddress(request);
        return new AuditEntry(userId, action, resourceType, resourceId, details, ipAddress);
    }

    public static String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
