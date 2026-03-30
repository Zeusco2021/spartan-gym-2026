package com.spartangoldengym.iacoach.dto;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class ExerciseRecommendationRequest {

    @NotNull
    private UUID userId;

    private List<String> targetMuscleGroups;
    private String difficulty;
    private List<String> availableEquipment;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public List<String> getTargetMuscleGroups() { return targetMuscleGroups; }
    public void setTargetMuscleGroups(List<String> targetMuscleGroups) { this.targetMuscleGroups = targetMuscleGroups; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public List<String> getAvailableEquipment() { return availableEquipment; }
    public void setAvailableEquipment(List<String> availableEquipment) { this.availableEquipment = availableEquipment; }
}
