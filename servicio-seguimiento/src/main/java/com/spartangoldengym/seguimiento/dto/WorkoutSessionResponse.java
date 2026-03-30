package com.spartangoldengym.seguimiento.dto;

import com.spartangoldengym.seguimiento.model.WorkoutSession;

import java.time.Instant;
import java.util.List;

public class WorkoutSessionResponse {

    private String sessionId;
    private String userId;
    private Instant startedAt;
    private Instant completedAt;
    private List<String> exercises;
    private long totalDurationSeconds;
    private double caloriesBurned;
    private String status;

    public static WorkoutSessionResponse fromModel(WorkoutSession session) {
        WorkoutSessionResponse resp = new WorkoutSessionResponse();
        resp.setSessionId(session.getSessionId());
        resp.setUserId(session.getUserId());
        resp.setStartedAt(session.getStartedAt());
        resp.setCompletedAt(session.getCompletedAt());
        resp.setExercises(session.getExercises());
        resp.setTotalDurationSeconds(session.getTotalDurationSeconds());
        resp.setCaloriesBurned(session.getCaloriesBurned());
        resp.setStatus(session.getStatus());
        return resp;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

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
