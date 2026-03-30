package com.spartangoldengym.iacoach.dto;

import java.util.List;
import java.util.UUID;

public class ExerciseRecommendationResponse {

    private UUID exerciseId;
    private String name;
    private List<String> muscleGroups;
    private List<String> equipmentRequired;
    private String difficulty;
    private int suggestedSets;
    private String suggestedReps;
    private Double suggestedWeight;
    private String rationale;

    public UUID getExerciseId() { return exerciseId; }
    public void setExerciseId(UUID exerciseId) { this.exerciseId = exerciseId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<String> getMuscleGroups() { return muscleGroups; }
    public void setMuscleGroups(List<String> muscleGroups) { this.muscleGroups = muscleGroups; }
    public List<String> getEquipmentRequired() { return equipmentRequired; }
    public void setEquipmentRequired(List<String> equipmentRequired) { this.equipmentRequired = equipmentRequired; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public int getSuggestedSets() { return suggestedSets; }
    public void setSuggestedSets(int suggestedSets) { this.suggestedSets = suggestedSets; }
    public String getSuggestedReps() { return suggestedReps; }
    public void setSuggestedReps(String suggestedReps) { this.suggestedReps = suggestedReps; }
    public Double getSuggestedWeight() { return suggestedWeight; }
    public void setSuggestedWeight(Double suggestedWeight) { this.suggestedWeight = suggestedWeight; }
    public String getRationale() { return rationale; }
    public void setRationale(String rationale) { this.rationale = rationale; }
}
