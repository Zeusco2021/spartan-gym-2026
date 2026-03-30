package com.spartangoldengym.iacoach.dto;

import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request DTO for overtraining detection.
 * Contains biometric data: resting heart rate, heart rate variability, and sleep quality.
 * Req 3.3, 18.3
 */
public class OvertrainingCheckRequest {

    @NotNull
    private UUID userId;

    private Double restingHeartRate;

    private Double heartRateVariability;

    private Double sleepQualityScore;

    private Double performanceChange;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public Double getRestingHeartRate() { return restingHeartRate; }
    public void setRestingHeartRate(Double restingHeartRate) { this.restingHeartRate = restingHeartRate; }
    public Double getHeartRateVariability() { return heartRateVariability; }
    public void setHeartRateVariability(Double heartRateVariability) { this.heartRateVariability = heartRateVariability; }
    public Double getSleepQualityScore() { return sleepQualityScore; }
    public void setSleepQualityScore(Double sleepQualityScore) { this.sleepQualityScore = sleepQualityScore; }
    public Double getPerformanceChange() { return performanceChange; }
    public void setPerformanceChange(Double performanceChange) { this.performanceChange = performanceChange; }
}
