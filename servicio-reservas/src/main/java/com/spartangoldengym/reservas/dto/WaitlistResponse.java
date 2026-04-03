package com.spartangoldengym.reservas.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class WaitlistResponse {

    private UUID classId;
    private int totalWaiting;
    private List<WaitlistEntry> entries;

    public UUID getClassId() { return classId; }
    public void setClassId(UUID classId) { this.classId = classId; }
    public int getTotalWaiting() { return totalWaiting; }
    public void setTotalWaiting(int totalWaiting) { this.totalWaiting = totalWaiting; }
    public List<WaitlistEntry> getEntries() { return entries; }
    public void setEntries(List<WaitlistEntry> entries) { this.entries = entries; }

    public static class WaitlistEntry {
        private UUID userId;
        private int position;
        private Instant addedAt;

        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        public int getPosition() { return position; }
        public void setPosition(int position) { this.position = position; }
        public Instant getAddedAt() { return addedAt; }
        public void setAddedAt(Instant addedAt) { this.addedAt = addedAt; }
    }
}
