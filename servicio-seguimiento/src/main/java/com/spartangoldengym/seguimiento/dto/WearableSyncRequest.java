package com.spartangoldengym.seguimiento.dto;

import java.time.Instant;
import java.util.List;

/**
 * Request to sync pending biometric data from a wearable device.
 * Supports offline-collected data that is synced when connection is restored.
 *
 * Validates: Requirements 8.2, 8.5
 */
public class WearableSyncRequest {

    private String userId;
    private String deviceType;
    private List<BiometricEntry> data;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public List<BiometricEntry> getData() { return data; }
    public void setData(List<BiometricEntry> data) { this.data = data; }

    /**
     * A single biometric data entry within a sync batch.
     */
    public static class BiometricEntry {

        private String dataType; // "heart_rate", "steps", "calories", "sleep"
        private double value;
        private Instant timestamp;

        public String getDataType() { return dataType; }
        public void setDataType(String dataType) { this.dataType = dataType; }

        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }

        public Instant getTimestamp() { return timestamp; }
        public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    }
}
