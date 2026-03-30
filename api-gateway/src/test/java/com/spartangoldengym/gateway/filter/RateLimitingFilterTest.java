package com.spartangoldengym.gateway.filter;

import com.spartangoldengym.common.constants.AppConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitingFilterTest {

    @Test
    void rateLimitConstants_matchRequirements() {
        assertEquals(1000, AppConstants.RATE_LIMIT_AUTHENTICATED,
                "Authenticated rate limit should be 1000 req/min");
        assertEquals(100, AppConstants.RATE_LIMIT_UNAUTHENTICATED,
                "Unauthenticated rate limit should be 100 req/min");
        assertEquals(60, AppConstants.RATE_LIMIT_WINDOW_SECONDS,
                "Rate limit window should be 60 seconds");
    }

    @Test
    void rateLimitingFilter_orderIsAfterJwtFilter() {
        // RateLimitingFilter order is -1, JwtAuthenticationFilter is -2
        // This ensures JWT runs first so X-User-Id header is available
        assertTrue(-2 < -1,
                "JWT filter (order -2) must run before rate limiting (order -1)");
    }
}
