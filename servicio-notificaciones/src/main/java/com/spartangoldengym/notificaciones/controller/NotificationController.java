package com.spartangoldengym.notificaciones.controller;

import com.spartangoldengym.notificaciones.dto.*;
import com.spartangoldengym.notificaciones.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for notification preferences, history and scheduling.
 *
 * Validates: Requirements 22.1, 22.2, 22.3, 22.4, 22.5, 22.6, 22.7, 22.8
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Get notification preferences for the authenticated user.
     * Validates: Requirement 22.3
     */
    @GetMapping("/preferences")
    public ResponseEntity<NotificationPreferenceResponse> getPreferences(
            @RequestHeader("X-User-Id") String userId) {
        NotificationPreferenceResponse response = notificationService.getPreferences(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update notification preferences for the authenticated user.
     * Validates: Requirement 22.3
     */
    @PutMapping("/preferences")
    public ResponseEntity<NotificationPreferenceResponse> updatePreferences(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody NotificationPreferenceRequest request) {
        request.setUserId(userId);
        NotificationPreferenceResponse response = notificationService.updatePreferences(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get notification history with optional filtering by channel, status and limit.
     * Validates: Requirement 22.6
     */
    @GetMapping("/history")
    public ResponseEntity<List<NotificationHistoryResponse>> getHistory(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "50") int limit) {
        List<NotificationHistoryResponse> history = notificationService.getHistory(userId);

        List<NotificationHistoryResponse> filtered = history.stream()
                .filter(n -> channel == null || channel.equals(n.getChannel()))
                .filter(n -> status == null || status.equals(n.getStatus()))
                .limit(limit)
                .collect(Collectors.toList());

        return ResponseEntity.ok(filtered);
    }

    /**
     * Schedule a notification for future delivery.
     * Validates: Requirement 22.8
     */
    @PostMapping("/schedule")
    public ResponseEntity<NotificationHistoryResponse> scheduleNotification(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ScheduleNotificationRequest request) {
        request.setUserId(userId);
        NotificationHistoryResponse response = notificationService.scheduleNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
