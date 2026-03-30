package com.spartangoldengym.usuarios.service;

import com.spartangoldengym.usuarios.entity.AuditLog;
import com.spartangoldengym.usuarios.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logAction(UUID userId, String action, String resourceType,
                          String resourceId, String details, String ipAddress) {
        AuditLog entry = new AuditLog();
        entry.setUserId(userId);
        entry.setAction(action);
        entry.setResourceType(resourceType);
        entry.setResourceId(resourceId);
        entry.setDetails(details);
        entry.setIpAddress(ipAddress);
        auditLogRepository.save(entry);
        log.info("Audit: user={} action={} resource={}:{}", userId, action, resourceType, resourceId);
    }

    public List<AuditLog> getAuditLogsForUser(UUID userId) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
