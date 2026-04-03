package com.spartangoldengym.analiticas.stream;

import com.spartangoldengym.analiticas.dto.EngagementAggregation;

/**
 * Serde for EngagementAggregation using simple delimited format.
 * Format: socialInteractionCount|nutritionLogCount
 */
public class EngagementAggregationSerde extends JsonSerde<EngagementAggregation> {

    public EngagementAggregationSerde() {
        super(EngagementAggregationSerde::serialize, EngagementAggregationSerde::deserialize);
    }

    static String serialize(EngagementAggregation agg) {
        return agg.getSocialInteractionCount() + "|" + agg.getNutritionLogCount();
    }

    static EngagementAggregation deserialize(String data) {
        String[] parts = data.split("\\|");
        if (parts.length < 2) return new EngagementAggregation();
        return new EngagementAggregation(
                Long.parseLong(parts[0]),
                Long.parseLong(parts[1])
        );
    }
}
