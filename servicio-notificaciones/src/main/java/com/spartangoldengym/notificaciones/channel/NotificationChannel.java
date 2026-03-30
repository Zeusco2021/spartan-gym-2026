package com.spartangoldengym.notificaciones.channel;

/**
 * Interface for notification delivery channels.
 * Implementations: Firebase Cloud Messaging (push), Amazon SES (email), Amazon SNS (SMS).
 * Validates: Requirement 22.1
 */
public interface NotificationChannel {

    /**
     * @return the channel identifier: "push", "email", or "sms"
     */
    String getChannelType();

    /**
     * Send a notification to the specified user.
     *
     * @param userId  target user identifier
     * @param title   notification title
     * @param content notification body
     * @return true if delivery was successful, false otherwise
     */
    boolean send(String userId, String title, String content);
}
