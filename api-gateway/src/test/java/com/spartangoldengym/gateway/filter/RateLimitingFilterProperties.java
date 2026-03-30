package com.spartangoldengym.gateway.filter;

import com.spartangoldengym.common.constants.AppConstants;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;
import org.mockito.Mockito;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for RateLimitingFilter.
 *
 * Feature: spartan-golden-gym, Property 35: API Gateway aplica rate limiting
 * Validates: Requirements 11.1, 13.6
 */
class RateLimitingFilterProperties {

    /**
     * **Validates: Requirements 11.1, 13.6**
     *
     * Property 35: API Gateway aplica rate limiting
     *
     * For any authenticated user making requests within the 1000 limit,
     * the request must be allowed through (not 429). For requests exceeding
     * the limit, the API Gateway must reject with HTTP 429.
     */
    @Property(tries = 100)
    @Tag("Feature: spartan-golden-gym, Property 35: API Gateway aplica rate limiting")
    void authenticatedUser_withinLimit_requestAllowed(
            @ForAll("userIds") String userId,
            @ForAll @IntRange(min = 1, max = 1000) int requestCount
    ) {
        ReactiveStringRedisTemplate redisTemplate = mock(ReactiveStringRedisTemplate.class);
        ReactiveValueOperations<String, String> valueOps = mock(ReactiveValueOperations.class);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        String expectedKey = "ratelimit:" + userId;
        when(valueOps.increment(expectedKey)).thenReturn(Mono.just((long) requestCount));
        if (requestCount == 1) {
            when(redisTemplate.expire(eq(expectedKey), any(Duration.class)))
                    .thenReturn(Mono.just(true));
        }
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        RateLimitingFilter filter = new RateLimitingFilter(redisTemplate);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("X-User-Id", userId)
                .build();
        ServerWebExchange exchange = org.springframework.mock.web.server.MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        // Request within limit should be forwarded to chain
        verify(chain, times(1)).filter(any(ServerWebExchange.class));

        // Verify response headers
        HttpHeaders responseHeaders = exchange.getResponse().getHeaders();
        assertEquals(String.valueOf(AppConstants.RATE_LIMIT_AUTHENTICATED),
                responseHeaders.getFirst("X-RateLimit-Limit"));
        int expectedRemaining = Math.max(0, AppConstants.RATE_LIMIT_AUTHENTICATED - requestCount);
        assertEquals(String.valueOf(expectedRemaining),
                responseHeaders.getFirst("X-RateLimit-Remaining"));
    }

    /**
     * **Validates: Requirements 11.1, 13.6**
     *
     * For any authenticated user exceeding 1000 requests per minute,
     * the API Gateway must reject with HTTP 429.
     */
    @Property(tries = 100)
    @Tag("Feature: spartan-golden-gym, Property 35: API Gateway aplica rate limiting")
    void authenticatedUser_exceedsLimit_rejected429(
            @ForAll("userIds") String userId,
            @ForAll @IntRange(min = 1001, max = 2000) int requestCount
    ) {
        ReactiveStringRedisTemplate redisTemplate = mock(ReactiveStringRedisTemplate.class);
        ReactiveValueOperations<String, String> valueOps = mock(ReactiveValueOperations.class);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        String expectedKey = "ratelimit:" + userId;
        when(valueOps.increment(expectedKey)).thenReturn(Mono.just((long) requestCount));
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        RateLimitingFilter filter = new RateLimitingFilter(redisTemplate);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("X-User-Id", userId)
                .build();
        ServerWebExchange exchange = org.springframework.mock.web.server.MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        // Request over limit should NOT be forwarded
        verify(chain, never()).filter(any(ServerWebExchange.class));

        // Response should be 429
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exchange.getResponse().getStatusCode());

        // Verify headers still set
        HttpHeaders responseHeaders = exchange.getResponse().getHeaders();
        assertEquals(String.valueOf(AppConstants.RATE_LIMIT_AUTHENTICATED),
                responseHeaders.getFirst("X-RateLimit-Limit"));
        assertEquals("0", responseHeaders.getFirst("X-RateLimit-Remaining"));
    }

    /**
     * **Validates: Requirements 11.1, 13.6**
     *
     * For any unauthenticated IP making requests within the 100 limit,
     * the request must be allowed through.
     */
    @Property(tries = 100)
    @Tag("Feature: spartan-golden-gym, Property 35: API Gateway aplica rate limiting")
    void unauthenticatedIp_withinLimit_requestAllowed(
            @ForAll("ipAddresses") String ipAddress,
            @ForAll @IntRange(min = 1, max = 100) int requestCount
    ) {
        ReactiveStringRedisTemplate redisTemplate = mock(ReactiveStringRedisTemplate.class);
        ReactiveValueOperations<String, String> valueOps = mock(ReactiveValueOperations.class);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        String expectedKey = "ratelimit:ip:" + ipAddress;
        when(valueOps.increment(expectedKey)).thenReturn(Mono.just((long) requestCount));
        if (requestCount == 1) {
            when(redisTemplate.expire(eq(expectedKey), any(Duration.class)))
                    .thenReturn(Mono.just(true));
        }
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        RateLimitingFilter filter = new RateLimitingFilter(redisTemplate);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("X-Forwarded-For", ipAddress)
                .build();
        ServerWebExchange exchange = org.springframework.mock.web.server.MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain, times(1)).filter(any(ServerWebExchange.class));

        HttpHeaders responseHeaders = exchange.getResponse().getHeaders();
        assertEquals(String.valueOf(AppConstants.RATE_LIMIT_UNAUTHENTICATED),
                responseHeaders.getFirst("X-RateLimit-Limit"));
        int expectedRemaining = Math.max(0, AppConstants.RATE_LIMIT_UNAUTHENTICATED - requestCount);
        assertEquals(String.valueOf(expectedRemaining),
                responseHeaders.getFirst("X-RateLimit-Remaining"));
    }

    /**
     * **Validates: Requirements 11.1, 13.6**
     *
     * For any unauthenticated IP exceeding 100 requests per minute,
     * the API Gateway must reject with HTTP 429.
     */
    @Property(tries = 100)
    @Tag("Feature: spartan-golden-gym, Property 35: API Gateway aplica rate limiting")
    void unauthenticatedIp_exceedsLimit_rejected429(
            @ForAll("ipAddresses") String ipAddress,
            @ForAll @IntRange(min = 101, max = 500) int requestCount
    ) {
        ReactiveStringRedisTemplate redisTemplate = mock(ReactiveStringRedisTemplate.class);
        ReactiveValueOperations<String, String> valueOps = mock(ReactiveValueOperations.class);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        String expectedKey = "ratelimit:ip:" + ipAddress;
        when(valueOps.increment(expectedKey)).thenReturn(Mono.just((long) requestCount));
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        RateLimitingFilter filter = new RateLimitingFilter(redisTemplate);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("X-Forwarded-For", ipAddress)
                .build();
        ServerWebExchange exchange = org.springframework.mock.web.server.MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain, never()).filter(any(ServerWebExchange.class));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exchange.getResponse().getStatusCode());

        HttpHeaders responseHeaders = exchange.getResponse().getHeaders();
        assertEquals(String.valueOf(AppConstants.RATE_LIMIT_UNAUTHENTICATED),
                responseHeaders.getFirst("X-RateLimit-Limit"));
        assertEquals("0", responseHeaders.getFirst("X-RateLimit-Remaining"));
    }

    /**
     * **Validates: Requirements 11.1, 13.6**
     *
     * Different users/IPs have independent rate limits — rate limiting
     * for one user does not affect another user's counter.
     */
    @Property(tries = 100)
    @Tag("Feature: spartan-golden-gym, Property 35: API Gateway aplica rate limiting")
    void differentUsers_haveIndependentLimits(
            @ForAll("userIds") String userA,
            @ForAll("userIds") String userB
    ) {
        Assume.that(!userA.equals(userB));

        ReactiveStringRedisTemplate redisTemplate = mock(ReactiveStringRedisTemplate.class);
        ReactiveValueOperations<String, String> valueOps = mock(ReactiveValueOperations.class);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        // User A is over the limit
        String keyA = "ratelimit:" + userA;
        when(valueOps.increment(keyA)).thenReturn(Mono.just(1500L));

        // User B is within the limit
        String keyB = "ratelimit:" + userB;
        when(valueOps.increment(keyB)).thenReturn(Mono.just(1L));
        when(redisTemplate.expire(eq(keyB), any(Duration.class))).thenReturn(Mono.just(true));

        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        RateLimitingFilter filter = new RateLimitingFilter(redisTemplate);

        // User A request — should be rejected
        MockServerHttpRequest requestA = MockServerHttpRequest.get("/api/test")
                .header("X-User-Id", userA)
                .build();
        ServerWebExchange exchangeA = org.springframework.mock.web.server.MockServerWebExchange.from(requestA);
        filter.filter(exchangeA, chain).block();
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exchangeA.getResponse().getStatusCode());

        // User B request — should be allowed
        MockServerHttpRequest requestB = MockServerHttpRequest.get("/api/test")
                .header("X-User-Id", userB)
                .build();
        ServerWebExchange exchangeB = org.springframework.mock.web.server.MockServerWebExchange.from(requestB);
        filter.filter(exchangeB, chain).block();

        // Chain should have been called exactly once (for user B only)
        verify(chain, times(1)).filter(any(ServerWebExchange.class));
    }

    /**
     * **Validates: Requirements 11.1, 13.6**
     *
     * When Redis is unavailable (error), the filter should fail-open
     * and allow the request through.
     */
    @Property(tries = 100)
    @Tag("Feature: spartan-golden-gym, Property 35: API Gateway aplica rate limiting")
    void redisError_failsOpen_requestAllowed(
            @ForAll("userIds") String userId
    ) {
        ReactiveStringRedisTemplate redisTemplate = mock(ReactiveStringRedisTemplate.class);
        ReactiveValueOperations<String, String> valueOps = mock(ReactiveValueOperations.class);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment(anyString()))
                .thenReturn(Mono.error(new RuntimeException("Redis connection refused")));
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        RateLimitingFilter filter = new RateLimitingFilter(redisTemplate);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("X-User-Id", userId)
                .build();
        ServerWebExchange exchange = org.springframework.mock.web.server.MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        // On Redis error, request should be allowed through (fail-open)
        verify(chain, times(1)).filter(any(ServerWebExchange.class));
    }

    // --- Arbitraries ---

    @Provide
    Arbitrary<String> userIds() {
        return Arbitraries.strings()
                .alpha()
                .numeric()
                .ofMinLength(3)
                .ofMaxLength(36);
    }

    @Provide
    Arbitrary<String> ipAddresses() {
        return Arbitraries.integers().between(1, 255).array(int[].class).ofSize(4)
                .map(octets -> octets[0] + "." + octets[1] + "." + octets[2] + "." + octets[3]);
    }
}
