package com.spartangoldengym.analiticas.repository;

import com.spartangoldengym.analiticas.dto.KpiData;
import com.spartangoldengym.analiticas.dto.ReportResponse;

import java.util.List;
import java.util.UUID;

public interface AnalyticsRepository {

    KpiData getRetentionRate();

    KpiData getWorkoutFrequency();

    KpiData getRevenue();

    KpiData getGymOccupancy();

    List<ReportResponse> getReports(String type);

    void saveReport(ReportResponse report);

    void saveKpiSnapshot(String metricName, double value, double changePercentage, String trend);
}
