package com.spartangoldengym.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * Circuit breaker configuration for the API Gateway.
 * Resilience4j circuit breaker instances are configured in application.yml:
 * - slidingWindowSize: 10 (count-based)
 * - failureRateThreshold: 50%
 * - waitDurationInOpenState: 30s
 * - permittedNumberOfCallsInHalfOpenState: 3
 */
@Configuration
public class CircuitBreakerConfig {

    @Bean
    public RouterFunction<ServerResponse> fallbackRoute() {
        return RouterFunctions.route()
                .GET("/fallback", request ->
                        ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("{\"error\":\"Service Unavailable\","
                                        + "\"message\":\"The requested service is temporarily "
                                        + "unavailable. Please try again later.\"}"))
                .POST("/fallback", request ->
                        ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("{\"error\":\"Service Unavailable\","
                                        + "\"message\":\"The requested service is temporarily "
                                        + "unavailable. Please try again later.\"}"))
                .PUT("/fallback", request ->
                        ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("{\"error\":\"Service Unavailable\","
                                        + "\"message\":\"The requested service is temporarily "
                                        + "unavailable. Please try again later.\"}"))
                .DELETE("/fallback", request ->
                        ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("{\"error\":\"Service Unavailable\","
                                        + "\"message\":\"The requested service is temporarily "
                                        + "unavailable. Please try again later.\"}"))
                .build();
    }
}
