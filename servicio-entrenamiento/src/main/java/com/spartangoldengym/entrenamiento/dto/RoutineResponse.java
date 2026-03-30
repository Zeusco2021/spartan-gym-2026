package com.spartangoldengym.entrenamiento.dto;

import java.util.List;
import java.util.UUID;

public class RoutineResponse {

    private UUID id;
    private UUID planId;
    private String name;
    private Integer dayOfWeek;
    private Integer sortOrder;
    private List<RoutineExerciseResponse> exercises;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPlanId() { return planId; }
    public void setPlanId(UUID planId) { this.planId = planId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public List<RoutineExerciseResponse> getExercises() { return exercises; }
    public void setExercises(List<RoutineExerciseResponse> exercises) { this.exercises = exercises; }

    public static class RoutineExerciseResponse {
        private UUID id;
        private UUID exerciseId;
        private String exerciseName;
        private Integer sets;
        private String reps;
        private Integer restSeconds;
        private Integer sortOrder;

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public UUID getExerciseId() { return exerciseId; }
        public void setExerciseId(UUID exerciseId) { this.exerciseId = exerciseId; }
        public String getExerciseName() { return exerciseName; }
        public void setExerciseName(String exerciseName) { this.exerciseName = exerciseName; }
        public Integer getSets() { return sets; }
        public void setSets(Integer sets) { this.sets = sets; }
        public String getReps() { return reps; }
        public void setReps(String reps) { this.reps = reps; }
        public Integer getRestSeconds() { return restSeconds; }
        public void setRestSeconds(Integer restSeconds) { this.restSeconds = restSeconds; }
        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    }
}
