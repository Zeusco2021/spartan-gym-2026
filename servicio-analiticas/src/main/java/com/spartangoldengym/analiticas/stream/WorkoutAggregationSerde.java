package com.spartangoldengym.analiticas.stream;

import com.spartangoldengym.analiticas.dto.WorkoutAggregation;

/**
 * Serde for WorkoutAggregation using simple delimited format.
 * Format: workoutCount|totalVolume|totalDurationSeconds
 */
public class WorkoutAggregationSerde extends JsonSerde<WorkoutAggregation> {

    public WorkoutAggregationSerde() {
        super(WorkoutAggregationSerde::serialize, WorkoutAggregationSerde::deserialize);
    }

    static String serialize(WorkoutAggregation agg) {
        return agg.getWorkoutCount() + "|" + agg.getTotalVolume() + "|" + agg.getTotalDurationSeconds();
    }

    static WorkoutAggregation deserialize(String data) {
        String[] parts = data.split("\\|");
        if (parts.length < 3) return new WorkoutAggregation();
        return new WorkoutAggregation(
                Long.parseLong(parts[0]),
                Double.parseDouble(parts[1]),
                Long.parseLong(parts[2])
        );
    }
}
