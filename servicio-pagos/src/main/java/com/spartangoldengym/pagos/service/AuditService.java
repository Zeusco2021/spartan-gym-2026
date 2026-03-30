package com.spartangoldengym.pagos.service;

import com.spartangoldengym.pagos.entity.AuditLog;
import com.spartangoldengym.pagos.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(UUID userId, String action, String resourceType, String resourceId, String details) {
        AuditLog entry = new AuditLog();
        entry.setUserId(userId);
        entry.setAction(action);
        entry.setResourceType(resourceType);
        entry.setResourceId(resourceId);
        entry.setDetails(details);
        auditLogRepository.save(entry);
    }
}
