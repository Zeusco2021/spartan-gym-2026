package com.spartangoldengym.iacoach.dto;

import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request DTO for adherence prediction.
 * Req 3.10, 18.1
 */
public class AdherencePredictionRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private UUID planId;

    private Integer completedWorkouts;

    private Integer totalPlannedWorkouts;

    private Double averageCompletionRate;

    private Integer streakDays;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getPlanId() { return planId; }
    public void setPlanId(UUID planId) { this.planId = planId; }
    public Integer getCompletedWorkouts() { return completedWorkouts; }
    public void setCompletedWorkouts(Integer completedWorkouts) { this.completedWorkouts = completedWorkouts; }
    public Integer getTotalPlannedWorkouts() { return totalPlannedWorkouts; }
    public void setTotalPlannedWorkouts(Integer totalPlannedWorkouts) { this.totalPlannedWorkouts = totalPlannedWorkouts; }
    public Double getAverageCompletionRate() { return averageCompletionRate; }
    public void setAverageCompletionRate(Double averageCompletionRate) { this.averageCompletionRate = averageCompletionRate; }
    public Integer getStreakDays() { return streakDays; }
    public void setStreakDays(Integer streakDays) { this.streakDays = streakDays; }
}
