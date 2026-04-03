package com.spartangoldengym.reservas.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.time.LocalTime;
import java.util.List;

public class TrainerAvailabilityRequest {

    @NotEmpty
    @Valid
    private List<AvailabilitySlot> slots;

    public List<AvailabilitySlot> getSlots() { return slots; }
    public void setSlots(List<AvailabilitySlot> slots) { this.slots = slots; }

    public static class AvailabilitySlot {
        private Integer dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;

        public Integer getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }
        public LocalTime getStartTime() { return startTime; }
        public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
        public LocalTime getEndTime() { return endTime; }
        public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    }
}
