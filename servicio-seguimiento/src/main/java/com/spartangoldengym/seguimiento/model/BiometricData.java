package com.spartangoldengym.seguimiento.model;

import java.time.Instant;

/**
 * Represents a single biometric data point synced from a wearable device.
 * Stored in Timestream (biometric_data table).
 *
 * Validates: Requirements 8.2, 8.3
 */
public class BiometricData {

    private String userId;
    private String dataType; // "heart_rate", "steps", "calories", "sleep"
    private double value;
    private String source; // "apple_watch", "fitbit", "garmin"
    private Instant timestamp;

    public BiometricData() {
    }

    public BiometricData(String userId, String dataType, double value, String source, Instant timestamp) {
        this.userId = userId;
        this.dataType = dataType;
        this.value = value;
        this.source = source;
        this.timestamp = timestamp;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
