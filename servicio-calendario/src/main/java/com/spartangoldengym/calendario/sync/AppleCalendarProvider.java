package com.spartangoldengym.calendario.sync;

import com.spartangoldengym.calendario.dto.SyncResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Apple Calendar (CalDAV) bidirectional sync provider.
 * Stub implementation — actual Apple Calendar API integration requires credentials.
 */
@Component
public class AppleCalendarProvider implements ExternalCalendarProvider {

    private static final Logger log = LoggerFactory.getLogger(AppleCalendarProvider.class);

    @Override
    public String getProviderName() {
        return "apple";
    }

    @Override
    public SyncResponse sync(UUID userId, String accessToken) {
        log.info("Syncing Apple Calendar for user {}", userId);
        SyncResponse response = new SyncResponse();
        response.setProvider("apple");
        response.setStatus("synced");
        response.setEventsImported(0);
        response.setEventsExported(0);
        return response;
    }
}
