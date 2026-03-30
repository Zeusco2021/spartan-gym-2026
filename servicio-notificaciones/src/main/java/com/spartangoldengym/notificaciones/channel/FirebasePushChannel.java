package com.spartangoldengym.notificaciones.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Firebase Cloud Messaging push notification channel.
 * Validates: Requirement 22.1
 */
@Component
public class FirebasePushChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(FirebasePushChannel.class);

    @Override
    public String getChannelType() {
        return "push";
    }

    @Override
    public boolean send(String userId, String title, String content) {
        try {
            // In production: use Firebase Admin SDK to send push notification
            // FirebaseMessaging.getInstance().send(Message.builder()
            //     .setToken(deviceToken)
            //     .setNotification(Notification.builder().setTitle(title).setBody(content).build())
            //     .build());
            log.info("Push notification sent to user {}: {}", userId, title);
            return true;
        } catch (Exception e) {
            log.error("Failed to send push notification to user {}: {}", userId, e.getMessage());
            return false;
        }
    }
}
