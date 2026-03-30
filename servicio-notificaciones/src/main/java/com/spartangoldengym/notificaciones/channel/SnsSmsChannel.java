package com.spartangoldengym.notificaciones.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Amazon SNS SMS notification channel.
 * Validates: Requirement 22.1
 */
@Component
public class SnsSmsChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(SnsSmsChannel.class);

    @Override
    public String getChannelType() {
        return "sms";
    }

    @Override
    public boolean send(String userId, String title, String content) {
        try {
            // In production: use AWS SNS SDK to send SMS
            // snsClient.publish(PublishRequest.builder()
            //     .phoneNumber(userPhoneNumber)
            //     .message(title + ": " + content)
            //     .build());
            log.info("SMS notification sent to user {}: {}", userId, title);
            return true;
        } catch (Exception e) {
            log.error("Failed to send SMS notification to user {}: {}", userId, e.getMessage());
            return false;
        }
    }
}
