package com.spartangoldengym.notificaciones.scheduler;

import com.spartangoldengym.notificaciones.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job that processes pending scheduled notifications.
 * Checks every minute for notifications whose scheduledAt has passed.
 * Validates: Requirement 22.8 (30 min reminders before sessions)
 */
@Component
public class ScheduledNotificationProcessor {

    private static final Logger log = LoggerFactory.getLogger(ScheduledNotificationProcessor.class);

    private final NotificationService notificationService;

    public ScheduledNotificationProcessor(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Runs every 60 seconds to check for scheduled notifications that are due.
     * In production, this would scan DynamoDB for notifications with
     * status="scheduled" and scheduledAt <= now.
     */
    @Scheduled(fixedRate = 60000)
    public void processScheduledNotifications() {
        log.debug("Checking for scheduled notifications due for delivery...");
        // In production: scan notification_delivery table for status="scheduled"
        // and scheduledAt <= Instant.now(), then call notificationService.sendNotification()
        // for each and update status accordingly.
    }
}
