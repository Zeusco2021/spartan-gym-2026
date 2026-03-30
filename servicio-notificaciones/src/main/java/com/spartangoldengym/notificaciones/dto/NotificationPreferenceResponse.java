package com.spartangoldengym.notificaciones.dto;

import java.util.Map;

public class NotificationPreferenceResponse {

    private String userId;
    private Map<String, Map<String, Boolean>> categoryChannels;
    private boolean quietHoursEnabled;
    private String quietHoursStart;
    private String quietHoursEnd;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Map<String, Map<String, Boolean>> getCategoryChannels() { return categoryChannels; }
    public void setCategoryChannels(Map<String, Map<String, Boolean>> categoryChannels) { this.categoryChannels = categoryChannels; }

    public boolean isQuietHoursEnabled() { return quietHoursEnabled; }
    public void setQuietHoursEnabled(boolean quietHoursEnabled) { this.quietHoursEnabled = quietHoursEnabled; }

    public String getQuietHoursStart() { return quietHoursStart; }
    public void setQuietHoursStart(String quietHoursStart) { this.quietHoursStart = quietHoursStart; }

    public String getQuietHoursEnd() { return quietHoursEnd; }
    public void setQuietHoursEnd(String quietHoursEnd) { this.quietHoursEnd = quietHoursEnd; }
}
