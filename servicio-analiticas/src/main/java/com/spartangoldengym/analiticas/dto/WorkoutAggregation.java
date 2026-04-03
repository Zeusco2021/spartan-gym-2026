package com.spartangoldengym.analiticas.dto;

/**
 * Aggregated workout metrics per user.
 * Tracks workout count, total volume (weight × reps), and total duration.
 */
public class WorkoutAggregation {

    private long workoutCount;
    private double totalVolume;
    private long totalDurationSeconds;

    public WorkoutAggregation() {
    }

    public WorkoutAggregation(long workoutCount, double totalVolume, long totalDurationSeconds) {
        this.workoutCount = workoutCount;
        this.totalVolume = totalVolume;
        this.totalDurationSeconds = totalDurationSeconds;
    }

    public long getWorkoutCount() { return workoutCount; }
    public void setWorkoutCount(long workoutCount) { this.workoutCount = workoutCount; }
    public double getTotalVolume() { return totalVolume; }
    public void setTotalVolume(double totalVolume) { this.totalVolume = totalVolume; }
    public long getTotalDurationSeconds() { return totalDurationSeconds; }
    public void setTotalDurationSeconds(long totalDurationSeconds) { this.totalDurationSeconds = totalDurationSeconds; }

    public double getAverageDurationSeconds() {
        return workoutCount > 0 ? (double) totalDurationSeconds / workoutCount : 0.0;
    }

    public WorkoutAggregation add(double volume, long durationSeconds) {
        this.workoutCount++;
        this.totalVolume += volume;
        this.totalDurationSeconds += durationSeconds;
        return this;
    }
}
