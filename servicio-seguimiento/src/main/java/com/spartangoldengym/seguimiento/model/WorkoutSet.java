package com.spartangoldengym.seguimiento.model;

import java.time.Instant;

/**
 * Represents a single set within a workout session, stored in DynamoDB.
 * PK: sessionId, SK: setId
 *
 * Validates: Requirement 4.3
 */
public class WorkoutSet {

    private String sessionId;
    private String setId;
    private String exerciseId;
    private double weight;
    private int reps;
    private int restSeconds;
    private Instant timestamp;

    public WorkoutSet() {}

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getSetId() { return setId; }
    public void setSetId(String setId) { this.setId = setId; }

    public String getExerciseId() { return exerciseId; }
    public void setExerciseId(String exerciseId) { this.exerciseId = exerciseId; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = reps; }

    public int getRestSeconds() { return restSeconds; }
    public void setRestSeconds(int restSeconds) { this.restSeconds = restSeconds; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
