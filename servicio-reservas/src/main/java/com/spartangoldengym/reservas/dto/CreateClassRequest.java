package com.spartangoldengym.reservas.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public class CreateClassRequest {

    @NotNull
    private UUID gymId;

    @NotNull
    private UUID instructorId;

    @NotBlank
    private String name;

    private String room;

    @NotNull
    @Min(1)
    private Integer maxCapacity;

    private String difficultyLevel;

    @NotNull
    private Instant scheduledAt;

    @NotNull
    @Min(1)
    private Integer durationMinutes;

    public UUID getGymId() { return gymId; }
    public void setGymId(UUID gymId) { this.gymId = gymId; }
    public UUID getInstructorId() { return instructorId; }
    public void setInstructorId(UUID instructorId) { this.instructorId = instructorId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
    public Integer getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(Integer maxCapacity) { this.maxCapacity = maxCapacity; }
    public String getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public Instant getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(Instant scheduledAt) { this.scheduledAt = scheduledAt; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
}
