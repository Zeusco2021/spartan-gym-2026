package com.spartangoldengym.analiticas.dto;

/**
 * Aggregated engagement metrics: social interactions and nutrition log frequency.
 */
public class EngagementAggregation {

    private long socialInteractionCount;
    private long nutritionLogCount;

    public EngagementAggregation() {
    }

    public EngagementAggregation(long socialInteractionCount, long nutritionLogCount) {
        this.socialInteractionCount = socialInteractionCount;
        this.nutritionLogCount = nutritionLogCount;
    }

    public long getSocialInteractionCount() { return socialInteractionCount; }
    public void setSocialInteractionCount(long socialInteractionCount) { this.socialInteractionCount = socialInteractionCount; }
    public long getNutritionLogCount() { return nutritionLogCount; }
    public void setNutritionLogCount(long nutritionLogCount) { this.nutritionLogCount = nutritionLogCount; }

    public EngagementAggregation addSocialInteraction() {
        this.socialInteractionCount++;
        return this;
    }

    public EngagementAggregation addNutritionLog() {
        this.nutritionLogCount++;
        return this;
    }
}
