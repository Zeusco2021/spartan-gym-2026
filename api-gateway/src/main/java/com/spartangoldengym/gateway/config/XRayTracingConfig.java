package com.spartangoldengym.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * AWS X-Ray distributed tracing configuration for the API Gateway.
 * In production, this integrates with the AWS X-Ray SDK to propagate
 * trace headers across microservices. This stub generates trace IDs
 * and propagates them via the X-Amzn-Trace-Id header.
 */
@Configuration
public class XRayTracingConfig {

    private static final Logger log = LoggerFactory.getLogger(XRayTracingConfig.class);
    private static final String TRACE_HEADER = "X-Amzn-Trace-Id";

    @Bean
    public GlobalFilter xRayTracingFilter() {
        return new XRayTracingFilter();
    }

    static class XRayTracingFilter implements GlobalFilter, Ordered {

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            String existingTraceId = exchange.getRequest().getHeaders()
                    .getFirst(TRACE_HEADER);

            String traceId;
            if (existingTraceId != null && !existingTraceId.isEmpty()) {
                traceId = existingTraceId;
            } else {
                traceId = generateTraceId();
            }

            exchange.getResponse().getHeaders().set(TRACE_HEADER, traceId);

            return chain.filter(
                    exchange.mutate()
                            .request(exchange.getRequest().mutate()
                                    .header(TRACE_HEADER, traceId)
                                    .build())
                            .build()
            );
        }

        private String generateTraceId() {
            long epochSeconds = System.currentTimeMillis() / 1000;
            String hexTime = Long.toHexString(epochSeconds);
            String uniqueId = UUID.randomUUID().toString().replace("-", "")
                    .substring(0, 24);
            return "Root=1-" + hexTime + "-" + uniqueId;
        }

        @Override
        public int getOrder() {
            return -3;
        }
    }
}
