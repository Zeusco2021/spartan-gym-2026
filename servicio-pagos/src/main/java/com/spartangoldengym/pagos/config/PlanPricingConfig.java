package com.spartangoldengym.pagos.config;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class PlanPricingConfig {

    private static final Map<String, BigDecimal> PLAN_PRICES;
    private static final Map<String, Integer> PLAN_DURATION_DAYS;
    private static final int REFUND_GUARANTEE_DAYS = 7;

    static {
        Map<String, BigDecimal> prices = new HashMap<>();
        prices.put("basic", new BigDecimal("9.99"));
        prices.put("premium", new BigDecimal("19.99"));
        prices.put("elite", new BigDecimal("29.99"));
        PLAN_PRICES = Collections.unmodifiableMap(prices);

        Map<String, Integer> durations = new HashMap<>();
        durations.put("basic", 30);
        durations.put("premium", 30);
        durations.put("elite", 30);
        PLAN_DURATION_DAYS = Collections.unmodifiableMap(durations);
    }

    public BigDecimal getPrice(String planType) {
        BigDecimal price = PLAN_PRICES.get(planType.toLowerCase());
        if (price == null) {
            throw new IllegalArgumentException("Unknown plan type: " + planType);
        }
        return price;
    }

    public int getDurationDays(String planType) {
        Integer days = PLAN_DURATION_DAYS.get(planType.toLowerCase());
        if (days == null) {
            throw new IllegalArgumentException("Unknown plan type: " + planType);
        }
        return days;
    }

    public int getRefundGuaranteeDays() {
        return REFUND_GUARANTEE_DAYS;
    }
}
