package com.spartangoldengym.notificaciones.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;

/**
 * Request DTO for scheduling a notification.
 * Validates: Requirement 22.8
 */
public class ScheduleNotificationRequest {

    @NotBlank
    private String userId;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @NotBlank
    private String category; // entrenamientos, social, pagos, nutricion

    @NotNull
    private Instant scheduledAt;

    private boolean urgent;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Instant getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(Instant scheduledAt) { this.scheduledAt = scheduledAt; }

    public boolean isUrgent() { return urgent; }
    public void setUrgent(boolean urgent) { this.urgent = urgent; }
}
