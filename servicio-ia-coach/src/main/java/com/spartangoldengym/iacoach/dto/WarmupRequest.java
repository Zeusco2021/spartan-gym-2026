package com.spartangoldengym.iacoach.dto;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class WarmupRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private List<String> plannedExerciseTypes;

    private List<String> targetMuscleGroups;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public List<String> getPlannedExerciseTypes() { return plannedExerciseTypes; }
    public void setPlannedExerciseTypes(List<String> plannedExerciseTypes) { this.plannedExerciseTypes = plannedExerciseTypes; }
    public List<String> getTargetMuscleGroups() { return targetMuscleGroups; }
    public void setTargetMuscleGroups(List<String> targetMuscleGroups) { this.targetMuscleGroups = targetMuscleGroups; }
}
