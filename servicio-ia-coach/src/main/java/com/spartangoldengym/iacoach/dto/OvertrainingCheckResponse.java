package com.spartangoldengym.iacoach.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for overtraining detection.
 * Contains risk level, alert flag, and rest recommendation.
 * Req 3.3, 18.3
 */
public class OvertrainingCheckResponse {

    private UUID userId;
    private double overtrainingRisk;
    private boolean restAlertGenerated;
    private String riskLevel;
    private String recommendation;
    private List<String> detectedIndicators;
    private int suggestedRestDays;
    private Instant checkedAt;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public double getOvertrainingRisk() { return overtrainingRisk; }
    public void setOvertrainingRisk(double overtrainingRisk) { this.overtrainingRisk = overtrainingRisk; }
    public boolean isRestAlertGenerated() { return restAlertGenerated; }
    public void setRestAlertGenerated(boolean restAlertGenerated) { this.restAlertGenerated = restAlertGenerated; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    public List<String> getDetectedIndicators() { return detectedIndicators; }
    public void setDetectedIndicators(List<String> detectedIndicators) { this.detectedIndicators = detectedIndicators; }
    public int getSuggestedRestDays() { return suggestedRestDays; }
    public void setSuggestedRestDays(int suggestedRestDays) { this.suggestedRestDays = suggestedRestDays; }
    public Instant getCheckedAt() { return checkedAt; }
    public void setCheckedAt(Instant checkedAt) { this.checkedAt = checkedAt; }
}
