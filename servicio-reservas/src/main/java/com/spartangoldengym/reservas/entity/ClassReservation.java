package com.spartangoldengym.reservas.entity;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "class_reservations")
public class ClassReservation {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "class_id", nullable = false)
    private UUID classId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 20)
    private String status = "confirmed";

    @Column(name = "penalty_count", nullable = false)
    private Integer penaltyCount = 0;

    @Column(name = "reserved_at", nullable = false)
    private Instant reservedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    public ClassReservation() {}

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
}
