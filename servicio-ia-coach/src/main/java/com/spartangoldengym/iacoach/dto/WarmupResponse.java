package com.spartangoldengym.iacoach.dto;

import java.util.List;
import java.util.UUID;

public class WarmupResponse {

    private UUID userId;
    private List<String> plannedExerciseTypes;
    private boolean warmupRecommended;
    private String warmupRationale;
    private List<WarmupExercise> warmupExercises;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public List<String> getPlannedExerciseTypes() { return plannedExerciseTypes; }
    public void setPlannedExerciseTypes(List<String> plannedExerciseTypes) { this.plannedExerciseTypes = plannedExerciseTypes; }
    public boolean isWarmupRecommended() { return warmupRecommended; }
    public void setWarmupRecommended(boolean warmupRecommended) { this.warmupRecommended = warmupRecommended; }
    public String getWarmupRationale() { return warmupRationale; }
    public void setWarmupRationale(String warmupRationale) { this.warmupRationale = warmupRationale; }
    public List<WarmupExercise> getWarmupExercises() { return warmupExercises; }
    public void setWarmupExercises(List<WarmupExercise> warmupExercises) { this.warmupExercises = warmupExercises; }

    public static class WarmupExercise {
        private String name;
        private int durationSeconds;
        private String description;
        private List<String> targetMuscleGroups;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getDurationSeconds() { return durationSeconds; }
        public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public List<String> getTargetMuscleGroups() { return targetMuscleGroups; }
        public void setTargetMuscleGroups(List<String> targetMuscleGroups) { this.targetMuscleGroups = targetMuscleGroups; }
    }
}
