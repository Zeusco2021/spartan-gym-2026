package com.spartangoldengym.iacoach.dto;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class AlternativeExerciseRequest {

    @NotNull
    private UUID exerciseId;

    @NotNull
    private UUID userId;

    private List<String> availableEquipment;

    public UUID getExerciseId() { return exerciseId; }
    public void setExerciseId(UUID exerciseId) { this.exerciseId = exerciseId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public List<String> getAvailableEquipment() { return availableEquipment; }
    public void setAvailableEquipment(List<String> availableEquipment) { this.availableEquipment = availableEquipment; }
}
