package com.spartangoldengym.usuarios.service;

import com.spartangoldengym.usuarios.entity.AuditLog;
import com.spartangoldengym.usuarios.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock private AuditLogRepository auditLogRepository;

    private AuditService auditService;

    @BeforeEach
    void setUp() {
        auditService = new AuditService(auditLogRepository);
    }

    @Test
    void logAction_persistsAuditEntry() {
        UUID userId = UUID.randomUUID();

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> {
            AuditLog log = inv.getArgument(0);
            log.setId(1L);
            return log;
        });

        auditService.logAction(userId, "PROFILE_UPDATE", "USER",
                userId.toString(), "{\"field\":\"name\"}", "192.168.1.1");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();

        assertEquals(userId, saved.getUserId());
        assertEquals("PROFILE_UPDATE", saved.getAction());
        assertEquals("USER", saved.getResourceType());
        assertEquals(userId.toString(), saved.getResourceId());
        assertEquals("{\"field\":\"name\"}", saved.getDetails());
        assertEquals("192.168.1.1", saved.getIpAddress());
    }

    @Test
    void getAuditLogsForUser_returnsLogs() {
        UUID userId = UUID.randomUUID();
        AuditLog log1 = new AuditLog();
        log1.setId(1L);
        log1.setUserId(userId);
        log1.setAction("PROFILE_VIEW");
        AuditLog log2 = new AuditLog();
        log2.setId(2L);
        log2.setUserId(userId);
        log2.setAction("PROFILE_UPDATE");

        when(auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(Arrays.asList(log2, log1));

        List<AuditLog> logs = auditService.getAuditLogsForUser(userId);

        assertEquals(2, logs.size());
        assertEquals("PROFILE_UPDATE", logs.get(0).getAction());
        assertEquals("PROFILE_VIEW", logs.get(1).getAction());
    }
}
