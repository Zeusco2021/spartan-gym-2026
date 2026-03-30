package com.spartangoldengym.entrenamiento.entity;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "exercises")
public class Exercise {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "muscle_groups", nullable = false, columnDefinition = "jsonb")
    private String muscleGroups;

    @Column(name = "equipment_required", columnDefinition = "jsonb")
    private String equipmentRequired;

    @Column(length = 20)
    private String difficulty;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(columnDefinition = "text")
    private String instructions;

    public Exercise() {}

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
