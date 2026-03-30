package com.spartangoldengym.gateway.filter;

import com.spartangoldengym.common.constants.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class RateLimitingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private final ReactiveStringRedisTemplate redisTemplate;

    public RateLimitingFilter(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        boolean authenticated = userId != null && !userId.isEmpty();

        String rateLimitKey;
        int maxRequests;

        if (authenticated) {
            rateLimitKey = "ratelimit:" + userId;
            maxRequests = AppConstants.RATE_LIMIT_AUTHENTICATED;
        } else {
            String clientIp = resolveClientIp(exchange);
            rateLimitKey = "ratelimit:ip:" + clientIp;
            maxRequests = AppConstants.RATE_LIMIT_UNAUTHENTICATED;
        }

        Duration window = Duration.ofSeconds(AppConstants.RATE_LIMIT_WINDOW_SECONDS);

        return redisTemplate.opsForValue().increment(rateLimitKey)
                .flatMap(count -> {
                    if (count == 1) {
                        return redisTemplate.expire(rateLimitKey, window)
                                .thenReturn(count);
                    }
                    return Mono.just(count);
                })
                .flatMap(count -> {
                    exchange.getResponse().getHeaders()
                            .set("X-RateLimit-Limit", String.valueOf(maxRequests));
                    exchange.getResponse().getHeaders()
                            .set("X-RateLimit-Remaining",
                                    String.valueOf(Math.max(0, maxRequests - count)));

                    if (count > maxRequests) {
                        log.debug("Rate limit exceeded for key: {}", rateLimitKey);
                        return rateLimitExceededResponse(exchange);
                    }
                    return chain.filter(exchange);
                })
                .onErrorResume(e -> {
                    log.warn("Redis unavailable for rate limiting, allowing request: {}",
                            e.getMessage());
                    return chain.filter(exchange);
                });
    }

    private String resolveClientIp(ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        return remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "unknown";
    }

    private Mono<Void> rateLimitExceededResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"error\":\"Too Many Requests\","
                + "\"message\":\"Rate limit exceeded. Please try again later.\"}";
        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
