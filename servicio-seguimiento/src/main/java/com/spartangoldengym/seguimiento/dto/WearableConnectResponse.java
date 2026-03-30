package com.spartangoldengym.seguimiento.dto;

import java.time.Instant;

/**
 * Response after connecting a wearable device.
 * Validates: Requirement 8.2
 */
public class WearableConnectResponse {

    private String connectionId;
    private String userId;
    private String deviceType;
    private String deviceId;
    private String status; // "connected", "disconnected"
    private Instant connectedAt;

    public String getConnectionId() { return connectionId; }
    public void setConnectionId(String connectionId) { this.connectionId = connectionId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getConnectedAt() { return connectedAt; }
    public void setConnectedAt(Instant connectedAt) { this.connectedAt = connectedAt; }
}
