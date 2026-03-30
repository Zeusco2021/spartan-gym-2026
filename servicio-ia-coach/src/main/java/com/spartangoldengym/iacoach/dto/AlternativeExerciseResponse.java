package com.spartangoldengym.iacoach.dto;

import java.util.List;
import java.util.UUID;

public class AlternativeExerciseResponse {

    private UUID originalExerciseId;
    private String originalExerciseName;
    private List<String> targetMuscleGroups;
    private List<Alternative> alternatives;

    public UUID getOriginalExerciseId() { return originalExerciseId; }
    public void setOriginalExerciseId(UUID originalExerciseId) { this.originalExerciseId = originalExerciseId; }
    public String getOriginalExerciseName() { return originalExerciseName; }
    public void setOriginalExerciseName(String originalExerciseName) { this.originalExerciseName = originalExerciseName; }
    public List<String> getTargetMuscleGroups() { return targetMuscleGroups; }
    public void setTargetMuscleGroups(List<String> targetMuscleGroups) { this.targetMuscleGroups = targetMuscleGroups; }
    public List<Alternative> getAlternatives() { return alternatives; }
    public void setAlternatives(List<Alternative> alternatives) { this.alternatives = alternatives; }

    public static class Alternative {
        private UUID exerciseId;
        private String name;
        private List<String> muscleGroups;
        private List<String> equipmentRequired;
        private String difficulty;

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
    }
}
