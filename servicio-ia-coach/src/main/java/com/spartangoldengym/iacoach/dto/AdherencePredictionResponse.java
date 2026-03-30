package com.spartangoldengym.iacoach.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for adherence prediction.
 * Req 3.10, 18.1
 */
public class AdherencePredictionResponse {

    private UUID userId;
    private UUID planId;
    private double adherenceProbability;
    private String riskLevel;
    private List<String> riskFactors;
    private List<String> suggestions;
    private Instant predictedAt;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getPlanId() { return planId; }
    public void setPlanId(UUID planId) { this.planId = planId; }
    public double getAdherenceProbability() { return adherenceProbability; }
    public void setAdherenceProbability(double adherenceProbability) { this.adherenceProbability = adherenceProbability; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public List<String> getRiskFactors() { return riskFactors; }
    public void setRiskFactors(List<String> riskFactors) { this.riskFactors = riskFactors; }
    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }
    public Instant getPredictedAt() { return predictedAt; }
    public void setPredictedAt(Instant predictedAt) { this.predictedAt = predictedAt; }
}
