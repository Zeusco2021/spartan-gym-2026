package com.spartangoldengym.iacoach.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class GeneratedPlanResponse {

    private UUID planId;
    private UUID userId;
    private String name;
    private String description;
    private List<RoutineDetail> routines;
    private boolean aiGenerated;
    private boolean noEquipment;
    private Instant createdAt;

    public UUID getPlanId() { return planId; }
    public void setPlanId(UUID planId) { this.planId = planId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<RoutineDetail> getRoutines() { return routines; }
    public void setRoutines(List<RoutineDetail> routines) { this.routines = routines; }
    public boolean isAiGenerated() { return aiGenerated; }
    public void setAiGenerated(boolean aiGenerated) { this.aiGenerated = aiGenerated; }
    public boolean isNoEquipment() { return noEquipment; }
    public void setNoEquipment(boolean noEquipment) { this.noEquipment = noEquipment; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static class RoutineDetail {
        private String name;
        private int dayOfWeek;
        private List<ExerciseDetail> exercises;
        private List<ExerciseDetail> warmupExercises;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }
        public List<ExerciseDetail> getExercises() { return exercises; }
        public void setExercises(List<ExerciseDetail> exercises) { this.exercises = exercises; }
        public List<ExerciseDetail> getWarmupExercises() { return warmupExercises; }
        public void setWarmupExercises(List<ExerciseDetail> warmupExercises) { this.warmupExercises = warmupExercises; }
    }

    public static class ExerciseDetail {
        private UUID exerciseId;
        private String name;
        private List<String> muscleGroups;
        private List<String> equipmentRequired;
        private int sets;
        private String reps;
        private int restSeconds;
        private Double suggestedWeight;

        public UUID getExerciseId() { return exerciseId; }
        public void setExerciseId(UUID exerciseId) { this.exerciseId = exerciseId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<String> getMuscleGroups() { return muscleGroups; }
        public void setMuscleGroups(List<String> muscleGroups) { this.muscleGroups = muscleGroups; }
        public List<String> getEquipmentRequired() { return equipmentRequired; }
        public void setEquipmentRequired(List<String> equipmentRequired) { this.equipmentRequired = equipmentRequired; }
        public int getSets() { return sets; }
        public void setSets(int sets) { this.sets = sets; }
        public String getReps() { return reps; }
        public void setReps(String reps) { this.reps = reps; }
        public int getRestSeconds() { return restSeconds; }
        public void setRestSeconds(int restSeconds) { this.restSeconds = restSeconds; }
        public Double getSuggestedWeight() { return suggestedWeight; }
        public void setSuggestedWeight(Double suggestedWeight) { this.suggestedWeight = suggestedWeight; }
    }
}
