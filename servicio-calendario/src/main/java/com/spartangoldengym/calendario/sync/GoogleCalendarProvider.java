package com.spartangoldengym.calendario.sync;

import com.spartangoldengym.calendario.dto.SyncResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Google Calendar bidirectional sync provider.
 * Stub implementation — actual Google Calendar API integration requires OAuth2 credentials.
 */
@Component
public class GoogleCalendarProvider implements ExternalCalendarProvider {

    private static final Logger log = LoggerFactory.getLogger(GoogleCalendarProvider.class);

    @Override
    public String getProviderName() {
        return "google";
    }

    @Override
    public SyncResponse sync(UUID userId, String accessToken) {
        log.info("Syncing Google Calendar for user {}", userId);
        // TODO: Integrate with Google Calendar API using accessToken
        SyncResponse response = new SyncResponse();
        response.setProvider("google");
        response.setStatus("synced");
        response.setEventsImported(0);
        response.setEventsExported(0);
        return response;
    }
}
