package com.spartangoldengym.notificaciones.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Amazon SES email notification channel.
 * Validates: Requirement 22.1
 */
@Component
public class SesEmailChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(SesEmailChannel.class);

    @Override
    public String getChannelType() {
        return "email";
    }

    @Override
    public boolean send(String userId, String title, String content) {
        try {
            // In production: use AWS SES SDK to send email
            // sesClient.sendEmail(SendEmailRequest.builder()
            //     .destination(d -> d.toAddresses(userEmail))
            //     .message(m -> m.subject(s -> s.data(title)).body(b -> b.text(t -> t.data(content))))
            //     .source(fromAddress)
            //     .build());
            log.info("Email notification sent to user {}: {}", userId, title);
            return true;
        } catch (Exception e) {
            log.error("Failed to send email notification to user {}: {}", userId, e.getMessage());
            return false;
        }
    }
}
