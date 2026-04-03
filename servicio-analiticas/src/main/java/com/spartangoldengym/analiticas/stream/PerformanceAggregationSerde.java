package com.spartangoldengym.analiticas.stream;

import com.spartangoldengym.analiticas.dto.PerformanceAggregation;

/**
 * Serde for PerformanceAggregation using simple delimited format.
 * Format: sampleCount|heartrateSum|heartrateMax|heartrateMin|exercisesCompleted|exercisesPlanned
 */
public class PerformanceAggregationSerde extends JsonSerde<PerformanceAggregation> {

    public PerformanceAggregationSerde() {
        super(PerformanceAggregationSerde::serialize, PerformanceAggregationSerde::deserialize);
    }

    static String serialize(PerformanceAggregation agg) {
        return agg.getSampleCount() + "|" + agg.getHeartrateSum() + "|"
                + agg.getHeartrateMax() + "|" + agg.getHeartrateMin() + "|"
                + agg.getExercisesCompleted() + "|" + agg.getExercisesPlanned();
    }

    static PerformanceAggregation deserialize(String data) {
        String[] parts = data.split("\\|");
        if (parts.length < 6) return new PerformanceAggregation();
        PerformanceAggregation agg = new PerformanceAggregation();
        agg.setSampleCount(Long.parseLong(parts[0]));
        agg.setHeartrateSum(Double.parseDouble(parts[1]));
        agg.setHeartrateMax(Double.parseDouble(parts[2]));
        agg.setHeartrateMin(Double.parseDouble(parts[3]));
        agg.setExercisesCompleted(Long.parseLong(parts[4]));
        agg.setExercisesPlanned(Long.parseLong(parts[5]));
        return agg;
    }
}
