package com.spartangoldengym.analiticas.dto;

import java.time.Instant;
import java.util.Map;

/**
 * Response DTO for the real-time Kafka Streams aggregated metrics endpoint.
 */
public class RealTimeMetricsResponse {

    private Map<String, WorkoutAggregation> workoutsByUser;
    private Map<String, PerformanceAggregation> performanceByUser;
    private Map<String, EngagementAggregation> engagementByUser;
    private Instant timestamp;

    public RealTimeMetricsResponse() {
    }

    public Map<String, WorkoutAggregation> getWorkoutsByUser() { return workoutsByUser; }
    public void setWorkoutsByUser(Map<String, WorkoutAggregation> workoutsByUser) { this.workoutsByUser = workoutsByUser; }
    public Map<String, PerformanceAggregation> getPerformanceByUser() { return performanceByUser; }
    public void setPerformanceByUser(Map<String, PerformanceAggregation> performanceByUser) { this.performanceByUser = performanceByUser; }
    public Map<String, EngagementAggregation> getEngagementByUser() { return engagementByUser; }
    public void setEngagementByUser(Map<String, EngagementAggregation> engagementByUser) { this.engagementByUser = engagementByUser; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
