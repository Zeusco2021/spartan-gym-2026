package com.spartangoldengym.iacoach.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for recommendation feedback recording.
 * Req 18.6
 */
public class RecommendationFeedbackResponse {

    private UUID feedbackId;
    private UUID userId;
    private UUID recommendationId;
    private String feedbackType;
    private boolean recorded;
    private Instant recordedAt;

    public UUID getFeedbackId() { return feedbackId; }
    public void setFeedbackId(UUID feedbackId) { this.feedbackId = feedbackId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getRecommendationId() { return recommendationId; }
    public void setRecommendationId(UUID recommendationId) { this.recommendationId = recommendationId; }
    public String getFeedbackType() { return feedbackType; }
    public void setFeedbackType(String feedbackType) { this.feedbackType = feedbackType; }
    public boolean isRecorded() { return recorded; }
    public void setRecorded(boolean recorded) { this.recorded = recorded; }
    public Instant getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Instant recordedAt) { this.recordedAt = recordedAt; }
}
