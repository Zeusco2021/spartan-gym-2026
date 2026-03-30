package com.spartangoldengym.notificaciones.service;

import com.spartangoldengym.notificaciones.channel.NotificationChannel;
import com.spartangoldengym.notificaciones.dto.*;
import com.spartangoldengym.notificaciones.model.NotificationDelivery;
import com.spartangoldengym.notificaciones.model.NotificationPreference;
import com.spartangoldengym.notificaciones.repository.NotificationDeliveryRepository;
import com.spartangoldengym.notificaciones.repository.NotificationPreferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core notification service handling multichannel delivery, preferences, quiet hours,
 * retry logic, and scheduling.
 *
 * Validates: Requirements 22.1, 22.2, 22.3, 22.4, 22.5, 22.6, 22.7, 22.8
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final int MAX_PUSH_RETRIES = 3;

    private final NotificationDeliveryRepository deliveryRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final Map<String, NotificationChannel> channelMap;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public NotificationService(NotificationDeliveryRepository deliveryRepository,
                               NotificationPreferenceRepository preferenceRepository,
                               List<NotificationChannel> channels,
                               KafkaTemplate<String, String> kafkaTemplate) {
        this.deliveryRepository = deliveryRepository;
        this.preferenceRepository = preferenceRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.channelMap = new HashMap<>();
        for (NotificationChannel channel : channels) {
            channelMap.put(channel.getChannelType(), channel);
        }
    }

    // --- Preferences ---

    /**
     * Get notification preferences for a user.
     * Validates: Requirement 22.3
     */
    public NotificationPreferenceResponse getPreferences(String userId) {
        NotificationPreference pref = preferenceRepository.findByUserId(userId);
        return toPreferenceResponse(pref);
    }

    /**
     * Update notification preferences for a user.
     * Validates: Requirement 22.3
     */
    public NotificationPreferenceResponse updatePreferences(NotificationPreferenceRequest request) {
        NotificationPreference pref = new NotificationPreference();
        pref.setUserId(request.getUserId());
        pref.setCategoryChannels(request.getCategoryChannels());
        pref.setQuietHoursEnabled(request.isQuietHoursEnabled());
        if (request.getQuietHoursStart() != null) {
            pref.setQuietHoursStart(LocalTime.parse(request.getQuietHoursStart()));
        }
        if (request.getQuietHoursEnd() != null) {
            pref.setQuietHoursEnd(LocalTime.parse(request.getQuietHoursEnd()));
        }
        preferenceRepository.save(pref);
        return toPreferenceResponse(pref);
    }

    // --- History ---

    /**
     * Get notification history for a user.
     * Validates: Requirement 22.6
     */
    public List<NotificationHistoryResponse> getHistory(String userId) {
        return deliveryRepository.findByUserId(userId).stream()
                .map(this::toHistoryResponse)
                .collect(Collectors.toList());
    }

    // --- Schedule ---

    /**
     * Schedule a notification for future delivery.
     * Validates: Requirement 22.8
     */
    public NotificationHistoryResponse scheduleNotification(ScheduleNotificationRequest request) {
        String notificationId = UUID.randomUUID().toString();

        NotificationDelivery delivery = new NotificationDelivery();
        delivery.setUserId(request.getUserId());
        delivery.setNotificationId(notificationId);
        delivery.setTitle(request.getTitle());
        delivery.setContent(request.getContent());
        delivery.setCategory(request.getCategory());
        delivery.setUrgent(request.isUrgent());
        delivery.setStatus("scheduled");
        delivery.setScheduledAt(request.getScheduledAt());
        delivery.setRetryCount(0);

        deliveryRepository.save(delivery);
        log.info("Notification {} scheduled for user {} at {}", notificationId,
                request.getUserId(), request.getScheduledAt());

        return toHistoryResponse(delivery);
    }

    // --- Core send logic ---

    /**
     * Send a notification to a user, applying preference rules and quiet hours.
     * Called by Kafka consumers when events arrive.
     *
     * Validates: Requirements 22.2, 22.3, 22.4, 22.5, 22.7
     */
    public void sendNotification(String userId, String title, String content,
                                 String category, boolean urgent) {
        NotificationPreference pref = preferenceRepository.findByUserId(userId);

        // Check quiet hours for non-urgent notifications (Req 22.4)
        if (!urgent && isInQuietHours(pref)) {
            log.info("User {} is in quiet hours, retaining non-urgent notification: {}", userId, title);
            saveDelivery(userId, title, content, category, "retained", urgent, null);
            return;
        }

        // Apply preference rules per channel (Req 22.3, 22.5)
        boolean anySent = false;
        for (String channelType : new String[]{"push", "email", "sms"}) {
            if (pref.isChannelEnabled(category, channelType)) {
                NotificationChannel channel = channelMap.get(channelType);
                if (channel != null) {
                    boolean success = deliverWithRetry(channel, userId, title, content,
                            category, urgent);
                    anySent = anySent || success;
                }
            }
        }

        if (!anySent) {
            log.warn("No channels enabled or all deliveries failed for user {} category {}",
                    userId, category);
        }
    }

    /**
     * Deliver notification with retry logic for push channel.
     * Push: retry up to 3 times with exponential backoff; send to DLQ if persists.
     * Validates: Requirement 22.7
     */
    boolean deliverWithRetry(NotificationChannel channel, String userId,
                                     String title, String content,
                                     String category, boolean urgent) {
        int maxRetries = "push".equals(channel.getChannelType()) ? MAX_PUSH_RETRIES : 1;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            boolean success = channel.send(userId, title, content);
            if (success) {
                saveDelivery(userId, title, content, category, "sent", urgent,
                        channel.getChannelType());
                return true;
            }

            log.warn("Delivery attempt {}/{} failed for user {} via {}",
                    attempt, maxRetries, userId, channel.getChannelType());

            if (attempt < maxRetries) {
                // Exponential backoff: 1s, 2s, 4s...
                try {
                    Thread.sleep((long) Math.pow(2, attempt - 1) * 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // All retries exhausted — mark as failed and send to DLQ
        saveDelivery(userId, title, content, category, "failed", urgent,
                channel.getChannelType());

        if ("push".equals(channel.getChannelType())) {
            sendToDlq(userId, title, content, category);
        }

        return false;
    }

    /**
     * Deliver retained notifications after quiet hours end.
     */
    public void deliverRetainedNotifications(String userId) {
        List<NotificationDelivery> retained = deliveryRepository.findPendingByStatus(userId, "retained");
        for (NotificationDelivery delivery : retained) {
            sendNotification(userId, delivery.getTitle(), delivery.getContent(),
                    delivery.getCategory(), delivery.isUrgent());
        }
    }

    // --- Helpers ---

    boolean isInQuietHours(NotificationPreference pref) {
        if (!pref.isQuietHoursEnabled() || pref.getQuietHoursStart() == null
                || pref.getQuietHoursEnd() == null) {
            return false;
        }
        LocalTime now = LocalTime.now();
        LocalTime start = pref.getQuietHoursStart();
        LocalTime end = pref.getQuietHoursEnd();

        // Handle overnight quiet hours (e.g., 22:00 - 07:00)
        if (start.isAfter(end)) {
            return !now.isBefore(start) || now.isBefore(end);
        }
        return !now.isBefore(start) && now.isBefore(end);
    }

    private NotificationDelivery saveDelivery(String userId, String title, String content,
                                              String category, String status, boolean urgent,
                                              String channel) {
        NotificationDelivery delivery = new NotificationDelivery();
        delivery.setUserId(userId);
        delivery.setNotificationId(UUID.randomUUID().toString());
        delivery.setTitle(title);
        delivery.setContent(content);
        delivery.setCategory(category);
        delivery.setStatus(status);
        delivery.setUrgent(urgent);
        delivery.setChannel(channel);
        delivery.setSentAt("sent".equals(status) ? Instant.now() : null);
        delivery.setRetryCount(0);
        deliveryRepository.save(delivery);
        return delivery;
    }

    private void sendToDlq(String userId, String title, String content, String category) {
        String dlqMessage = String.format(
                "{\"userId\":\"%s\",\"title\":\"%s\",\"category\":\"%s\"}", userId, title, category);
        kafkaTemplate.send("notifications.dlq", userId, dlqMessage);
        log.error("Push notification sent to DLQ for user {}: {}", userId, title);
    }

    private NotificationPreferenceResponse toPreferenceResponse(NotificationPreference pref) {
        NotificationPreferenceResponse response = new NotificationPreferenceResponse();
        response.setUserId(pref.getUserId());
        response.setCategoryChannels(pref.getCategoryChannels());
        response.setQuietHoursEnabled(pref.isQuietHoursEnabled());
        response.setQuietHoursStart(pref.getQuietHoursStart() != null
                ? pref.getQuietHoursStart().toString() : null);
        response.setQuietHoursEnd(pref.getQuietHoursEnd() != null
                ? pref.getQuietHoursEnd().toString() : null);
        return response;
    }

    private NotificationHistoryResponse toHistoryResponse(NotificationDelivery delivery) {
        NotificationHistoryResponse response = new NotificationHistoryResponse();
        response.setNotificationId(delivery.getNotificationId());
        response.setChannel(delivery.getChannel());
        response.setCategory(delivery.getCategory());
        response.setStatus(delivery.getStatus());
        response.setTitle(delivery.getTitle());
        response.setContent(delivery.getContent());
        response.setUrgent(delivery.isUrgent());
        response.setSentAt(delivery.getSentAt());
        response.setReadAt(delivery.getReadAt());
        response.setRetryCount(delivery.getRetryCount());
        return response;
    }
}
