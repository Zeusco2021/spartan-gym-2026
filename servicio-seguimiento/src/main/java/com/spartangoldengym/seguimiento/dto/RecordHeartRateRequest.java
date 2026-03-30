package com.spartangoldengym.seguimiento.dto;

/**
 * Request to record heart rate data from a wearable device.
 * Validates: Requirement 4.2, 8.4
 */
public class RecordHeartRateRequest {

    private int bpm;
    private String deviceType;

    public int getBpm() { return bpm; }
    public void setBpm(int bpm) { this.bpm = bpm; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
}
