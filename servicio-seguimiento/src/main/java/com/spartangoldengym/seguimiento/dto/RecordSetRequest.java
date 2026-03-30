package com.spartangoldengym.seguimiento.dto;

/**
 * Request to record a completed set within a workout session.
 * Validates: Requirement 4.3
 */
public class RecordSetRequest {

    private String exerciseId;
    private double weight;
    private int reps;
    private int restSeconds;

    public String getExerciseId() { return exerciseId; }
    public void setExerciseId(String exerciseId) { this.exerciseId = exerciseId; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = reps; }

    public int getRestSeconds() { return restSeconds; }
    public void setRestSeconds(int restSeconds) { this.restSeconds = restSeconds; }
}
