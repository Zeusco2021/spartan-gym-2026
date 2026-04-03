package com.spartangoldengym.reservas.dto;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class TrainerAvailabilityResponse {

    private UUID trainerId;
    private List<AvailabilitySlot> slots;

    public UUID getTrainerId() { return trainerId; }
    public void setTrainerId(UUID trainerId) { this.trainerId = trainerId; }
    public List<AvailabilitySlot> getSlots() { return slots; }
    public void setSlots(List<AvailabilitySlot> slots) { this.slots = slots; }

    public static class AvailabilitySlot {
        private UUID id;
        private Integer dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public Integer getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }
        public LocalTime getStartTime() { return startTime; }
        public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
        public LocalTime getEndTime() { return endTime; }
        public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    }
}
