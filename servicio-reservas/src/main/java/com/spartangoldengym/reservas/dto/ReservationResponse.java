package com.spartangoldengym.reservas.dto;

import java.time.Instant;
import java.util.UUID;

public class ReservationResponse {

    private UUID id;
    private UUID classId;
    private UUID userId;
    private String status;
    private Integer penaltyCount;
    private Instant reservedAt;
    private Instant cancelledAt;
    private Integer waitlistPosition;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getClassId() { return classId; }
    public void setClassId(UUID classId) { this.classId = classId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getPenaltyCount() { return penaltyCount; }
    public void setPenaltyCount(Integer penaltyCount) { this.penaltyCount = penaltyCount; }
    public Instant getReservedAt() { return reservedAt; }
    public void setReservedAt(Instant reservedAt) { this.reservedAt = reservedAt; }
    public Instant getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; }
    public Integer getWaitlistPosition() { return waitlistPosition; }
    public void setWaitlistPosition(Integer waitlistPosition) { this.waitlistPosition = waitlistPosition; }
}
