package com.spartangoldengym.seguimiento.dto;

/**
 * Request to connect/pair a wearable device.
 * Validates: Requirement 8.2
 */
public class WearableConnectRequest {

    private String userId;
    private String deviceType; // "apple_watch", "fitbit", "garmin"
    private String deviceId;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
}
