package com.spartangoldengym.social.dto;

import javax.validation.constraints.NotNull;
import java.util.UUID;

public class CompleteChallengeRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private UUID challengeId;

    private Double achievedValue;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getChallengeId() { return challengeId; }
    public void setChallengeId(UUID challengeId) { this.challengeId = challengeId; }
    public Double getAchievedValue() { return achievedValue; }
    public void setAchievedValue(Double achievedValue) { this.achievedValue = achievedValue; }
}
