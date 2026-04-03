package com.spartangoldengym.calendario.sync;

import com.spartangoldengym.calendario.dto.SyncResponse;

import java.util.UUID;

/**
 * Interface for external calendar providers (Google, Apple, Outlook).
 * Implementations handle bidirectional sync with their respective APIs.
 */
public interface ExternalCalendarProvider {

    String getProviderName();

    SyncResponse sync(UUID userId, String accessToken);
}
