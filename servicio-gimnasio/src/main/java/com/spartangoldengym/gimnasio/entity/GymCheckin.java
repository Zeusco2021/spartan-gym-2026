package com.spartangoldengym.gimnasio.entity;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "gym_checkins")
public class GymCheckin {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id")
    private Gym gym;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "checked_in_at")
    private Instant checkedInAt;

    @Column(name = "checked_out_at")
    private Instant checkedOutAt;

    @PrePersist
    protected void onCreate() {
        checkedInAt = Instant.now();
    }

    public GymCheckin() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Gym getGym() { return gym; }
    public void setGym(Gym gym) { this.gym = gym; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public Instant getCheckedInAt() { return checkedInAt; }
    public void setCheckedInAt(Instant checkedInAt) { this.checkedInAt = checkedInAt; }
    public Instant getCheckedOutAt() { return checkedOutAt; }
    public void setCheckedOutAt(Instant checkedOutAt) { this.checkedOutAt = checkedOutAt; }
}
