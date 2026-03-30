package com.spartangoldengym.entrenamiento.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class CreateRoutineRequest {

    @NotNull
    private UUID planId;

    @NotBlank
    private String name;

    private Integer dayOfWeek;

    private Integer sortOrder;

    private List<RoutineExerciseItem> exercises;

    public UUID getPlanId() { return planId; }
    public void setPlanId(UUID planId) { this.planId = planId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public List<RoutineExerciseItem> getExercises() { return exercises; }
    public void setExercises(List<RoutineExerciseItem> exercises) { this.exercises = exercises; }

    public static class RoutineExerciseItem {
        @NotNull
        private UUID exerciseId;
        @NotNull
        private Integer sets;
        private String reps;
        private Integer restSeconds;
        private Integer sortOrder;

        public UUID getExerciseId() { return exerciseId; }
        public void setExerciseId(UUID exerciseId) { this.exerciseId = exerciseId; }
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
