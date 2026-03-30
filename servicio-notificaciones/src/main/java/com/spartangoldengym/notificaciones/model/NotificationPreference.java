package com.spartangoldengym.notificaciones.model;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * User notification preferences per category and channel.
 * Stored in DynamoDB user_preferences table with preferenceKey = "notification_preferences".
 * Validates: Requirement 22.3
 */
public class NotificationPreference {

    private String userId;

    /**
     * Category preferences: key = category (entrenamientos, social, pagos, nutricion),
     * value = map of channel -> enabled (push, email, sms).
     */
    private Map<String, Map<String, Boolean>> categoryChannels = new HashMap<>();

    /** Quiet hours start (e.g., 22:00) */
    private LocalTime quietHoursStart;

    /** Quiet hours end (e.g., 07:00) */
    private LocalTime quietHoursEnd;

    /** Whether quiet hours are enabled */
    private boolean quietHoursEnabled;

    public NotificationPreference() {}

    public NotificationPreference(String userId) {
        this.userId = userId;
        // Default: all channels enabled for all categories
        for (String category : new String[]{"entrenamientos", "social", "pagos", "nutricion"}) {
            Map<String, Boolean> channels = new HashMap<>();
            channels.put("push", true);
            channels.put("email", true);
            channels.put("sms", false);
            categoryChannels.put(category, channels);
        }
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Map<String, Map<String, Boolean>> getCategoryChannels() { return categoryChannels; }
    public void setCategoryChannels(Map<String, Map<String, Boolean>> categoryChannels) { this.categoryChannels = categoryChannels; }

    public LocalTime getQuietHoursStart() { return quietHoursStart; }
    public void setQuietHoursStart(LocalTime quietHoursStart) { this.quietHoursStart = quietHoursStart; }

    public LocalTime getQuietHoursEnd() { return quietHoursEnd; }
    public void setQuietHoursEnd(LocalTime quietHoursEnd) { this.quietHoursEnd = quietHoursEnd; }

    public boolean isQuietHoursEnabled() { return quietHoursEnabled; }
    public void setQuietHoursEnabled(boolean quietHoursEnabled) { this.quietHoursEnabled = quietHoursEnabled; }

    /**
     * Check if a specific channel is enabled for a category.
     */
    public boolean isChannelEnabled(String category, String channel) {
        Map<String, Boolean> channels = categoryChannels.get(category);
        if (channels == null) {
            return false;
        }
        return Boolean.TRUE.equals(channels.get(channel));
    }
}
