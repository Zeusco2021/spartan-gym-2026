package com.spartangoldengym.iacoach.dto;

import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request DTO for recording user feedback on AI recommendations.
 * Req 18.6
 */
public class RecommendationFeedbackRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private UUID recommendationId;

    @NotNull
    private String feedbackType;

    private String comment;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getRecommendationId() { return recommendationId; }
    public void setRecommendationId(UUID recommendationId) { this.recommendationId = recommendationId; }
    public String getFeedbackType() { return feedbackType; }
    public void setFeedbackType(String feedbackType) { this.feedbackType = feedbackType; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
