package com.spartangoldengym.analiticas.dto;

import java.time.Instant;
import java.util.List;

public class DashboardResponse {

    private KpiData retentionRate;
    private KpiData workoutFrequency;
    private KpiData revenue;
    private KpiData gymOccupancy;
    private List<KpiData> additionalMetrics;
    private Instant generatedAt;

    public DashboardResponse() {
    }

    public KpiData getRetentionRate() { return retentionRate; }
    public void setRetentionRate(KpiData retentionRate) { this.retentionRate = retentionRate; }
    public KpiData getWorkoutFrequency() { return workoutFrequency; }
    public void setWorkoutFrequency(KpiData workoutFrequency) { this.workoutFrequency = workoutFrequency; }
    public KpiData getRevenue() { return revenue; }
    public void setRevenue(KpiData revenue) { this.revenue = revenue; }
    public KpiData getGymOccupancy() { return gymOccupancy; }
    public void setGymOccupancy(KpiData gymOccupancy) { this.gymOccupancy = gymOccupancy; }
    public List<KpiData> getAdditionalMetrics() { return additionalMetrics; }
    public void setAdditionalMetrics(List<KpiData> additionalMetrics) { this.additionalMetrics = additionalMetrics; }
    public Instant getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Instant generatedAt) { this.generatedAt = generatedAt; }
}
