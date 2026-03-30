package com.spartangoldengym.usuarios.dto;

import java.time.Instant;
import java.util.UUID;

public class DeletionResponse {

    private UUID userId;
    private String status;
    private Instant scheduledDeletionDate;
    private String message;

    public DeletionResponse() {}

    public DeletionResponse(UUID userId, String status, Instant scheduledDeletionDate, String message) {
        this.userId = userId;
        this.status = status;
        this.scheduledDeletionDate = scheduledDeletionDate;
        this.message = message;
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getScheduledDeletionDate() { return scheduledDeletionDate; }
    public void setScheduledDeletionDate(Instant scheduledDeletionDate) { this.scheduledDeletionDate = scheduledDeletionDate; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
