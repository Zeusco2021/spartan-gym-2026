package com.spartangoldengym.iacoach.model;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ExerciseInfo {

    private final UUID id;
    private final String name;
    private final List<String> muscleGroups;
    private final List<String> equipmentRequired;
    private final String difficulty;

    public ExerciseInfo(UUID id, String name, List<String> muscleGroups,
                        List<String> equipmentRequired, String difficulty) {
        this.id = id;
        this.name = name;
        this.muscleGroups = Collections.unmodifiableList(muscleGroups);
        this.equipmentRequired = equipmentRequired != null
                ? Collections.unmodifiableList(equipmentRequired)
                : Collections.emptyList();
        this.difficulty = difficulty;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public List<String> getMuscleGroups() { return muscleGroups; }
    public List<String> getEquipmentRequired() { return equipmentRequired; }
    public String getDifficulty() { return difficulty; }

    public boolean requiresEquipment() {
        return !equipmentRequired.isEmpty();
    }
}
