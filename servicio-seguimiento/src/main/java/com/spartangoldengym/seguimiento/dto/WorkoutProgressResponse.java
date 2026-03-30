package com.spartangoldengym.seguimiento.dto;

/**
 * Aggregated progress metrics for a user.
 * Validates: Requirement 4.6
 */
public class WorkoutProgressResponse {

    private String userId;
    private int totalWorkouts;
    private long totalDurationSeconds;
    private double totalCaloriesBurned;
    private double totalVolumeKg;
    private double averageDurationSeconds;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getTotalWorkouts() { return totalWorkouts; }
    public void setTotalWorkouts(int totalWorkouts) { this.totalWorkouts = totalWorkouts; }

    public long getTotalDurationSeconds() { return totalDurationSeconds; }
    public void setTotalDurationSeconds(long totalDurationSeconds) { this.totalDurationSeconds = totalDurationSeconds; }

    public double getTotalCaloriesBurned() { return totalCaloriesBurned; }
    public void setTotalCaloriesBurned(double totalCaloriesBurned) { this.totalCaloriesBurned = totalCaloriesBurned; }

    public double getTotalVolumeKg() { return totalVolumeKg; }
    public void setTotalVolumeKg(double totalVolumeKg) { this.totalVolumeKg = totalVolumeKg; }

    public double getAverageDurationSeconds() { return averageDurationSeconds; }
    public void setAverageDurationSeconds(double averageDurationSeconds) { this.averageDurationSeconds = averageDurationSeconds; }
}
