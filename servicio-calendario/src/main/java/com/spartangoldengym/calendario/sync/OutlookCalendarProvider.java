package com.spartangoldengym.calendario.sync;

import com.spartangoldengym.calendario.dto.SyncResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Outlook Calendar (Microsoft Graph) bidirectional sync provider.
 * Stub implementation — actual Outlook Calendar API integration requires credentials.
 */
@Component
public class OutlookCalendarProvider implements ExternalCalendarProvider {

    private static final Logger log = LoggerFactory.getLogger(OutlookCalendarProvider.class);

    @Override
    public String getProviderName() {
        return "outlook";
    }

    @Override
    public SyncResponse sync(UUID userId, String accessToken) {
        log.info("Syncing Outlook Calendar for user {}", userId);
        SyncResponse response = new SyncResponse();
        response.setProvider("outlook");
        response.setStatus("synced");
        response.setEventsImported(0);
        response.setEventsExported(0);
        return response;
    }
}
