package com.spartangoldengym.gimnasio.dto;

import java.time.Instant;
import java.util.UUID;

public class CheckinResponse {

    private UUID id;
    private UUID gymId;
    private UUID userId;
    private Instant checkedInAt;

    public CheckinResponse() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getGymId() { return gymId; }
    public void setGymId(UUID gymId) { this.gymId = gymId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public Instant getCheckedInAt() { return checkedInAt; }
    public void setCheckedInAt(Instant checkedInAt) { this.checkedInAt = checkedInAt; }
}
