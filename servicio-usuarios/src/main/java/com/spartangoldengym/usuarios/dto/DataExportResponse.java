package com.spartangoldengym.usuarios.dto;

import java.time.Instant;
import java.util.UUID;

public class DataExportResponse {

    private UUID userId;
    private String status;
    private Instant requestedAt;
    private String message;

    public DataExportResponse() {}

    public DataExportResponse(UUID userId, String status, String message) {
        this.userId = userId;
        this.status = status;
        this.requestedAt = Instant.now();
        this.message = message;
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getRequestedAt() { return requestedAt; }
    public void setRequestedAt(Instant requestedAt) { this.requestedAt = requestedAt; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
