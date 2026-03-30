package com.spartangoldengym.social.dto;

import java.util.UUID;

public class WorkoutSyncMessage {

    private UUID userId;
    private UUID sessionId;
    private String eventType; // "set_completed", "heartrate_update", "workout_started", "workout_completed"
    private String payload;

    public WorkoutSyncMessage() {}

    public WorkoutSyncMessage(UUID userId, UUID sessionId, String eventType, String payload) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.eventType = eventType;
        this.payload = payload;
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
}
