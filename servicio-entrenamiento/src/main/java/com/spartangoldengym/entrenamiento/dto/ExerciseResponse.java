package com.spartangoldengym.entrenamiento.dto;

import java.util.UUID;

public class ExerciseResponse {

    private UUID id;
    private String name;
    private String muscleGroups;
    private String equipmentRequired;
    private String difficulty;
    private String videoUrl;
    private String instructions;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMuscleGroups() { return muscleGroups; }
    public void setMuscleGroups(String muscleGroups) { this.muscleGroups = muscleGroups; }
    public String getEquipmentRequired() { return equipmentRequired; }
    public void setEquipmentRequired(String equipmentRequired) { this.equipmentRequired = equipmentRequired; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
}
