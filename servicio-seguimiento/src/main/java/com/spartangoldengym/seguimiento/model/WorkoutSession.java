package com.spartangoldengym.seguimiento.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a workout session stored in DynamoDB.
 * PK: userId, SK: sessionId
 *
 * Validates: Requirement 4.1
 */
public class WorkoutSession {

    private String userId;
    private String sessionId;
    private Instant startedAt;
    private Instant completedAt;
    private List<String> exercises;
    private long totalDurationSeconds;
    private double caloriesBurned;
    private String status; // "active", "completed"

    public WorkoutSession() {
        this.exercises = new ArrayList<>();
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public List<String> getExercises() { return exercises; }
    public void setExercises(List<String> exercises) { this.exercises = exercises; }

    public long getTotalDurationSeconds() { return totalDurationSeconds; }
    public void setTotalDurationSeconds(long totalDurationSeconds) { this.totalDurationSeconds = totalDurationSeconds; }

    public double getCaloriesBurned() { return caloriesBurned; }
    public void setCaloriesBurned(double caloriesBurned) { this.caloriesBurned = caloriesBurned; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
