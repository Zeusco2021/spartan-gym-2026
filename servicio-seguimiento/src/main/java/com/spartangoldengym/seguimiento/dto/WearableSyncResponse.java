package com.spartangoldengym.seguimiento.dto;

import java.time.Instant;

/**
 * Response after syncing biometric data from a wearable.
 * Validates: Requirements 8.2, 8.3
 */
public class WearableSyncResponse {

    private String userId;
    private int recordsSynced;
    private Instant syncedAt;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getRecordsSynced() { return recordsSynced; }
    public void setRecordsSynced(int recordsSynced) { this.recordsSynced = recordsSynced; }

    public Instant getSyncedAt() { return syncedAt; }
    public void setSyncedAt(Instant syncedAt) { this.syncedAt = syncedAt; }
}
