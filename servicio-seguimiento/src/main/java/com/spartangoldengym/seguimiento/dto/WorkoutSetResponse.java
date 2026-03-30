package com.spartangoldengym.seguimiento.dto;

import com.spartangoldengym.seguimiento.model.WorkoutSet;

import java.time.Instant;

public class WorkoutSetResponse {

    private String setId;
    private String sessionId;
    private String exerciseId;
    private double weight;
    private int reps;
    private int restSeconds;
    private Instant timestamp;

    public static WorkoutSetResponse fromModel(WorkoutSet set) {
        WorkoutSetResponse resp = new WorkoutSetResponse();
        resp.setSetId(set.getSetId());
        resp.setSessionId(set.getSessionId());
        resp.setExerciseId(set.getExerciseId());
        resp.setWeight(set.getWeight());
        resp.setReps(set.getReps());
        resp.setRestSeconds(set.getRestSeconds());
        resp.setTimestamp(set.getTimestamp());
        return resp;
    }

    public String getSetId() { return setId; }
    public void setSetId(String setId) { this.setId = setId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

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
