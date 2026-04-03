package com.spartangoldengym.analiticas.service;

import com.spartangoldengym.analiticas.dto.DashboardResponse;
import com.spartangoldengym.analiticas.dto.MetricsResponse;
import com.spartangoldengym.analiticas.dto.ReportResponse;
import com.spartangoldengym.analiticas.repository.AnalyticsRepository;
import com.spartangoldengym.analiticas.repository.InMemoryMetricsStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final AnalyticsRepository analyticsRepository;
    private final InMemoryMetricsStore metricsStore;

    public AnalyticsService(AnalyticsRepository analyticsRepository,
                            InMemoryMetricsStore metricsStore) {
        this.analyticsRepository = analyticsRepository;
        this.metricsStore = metricsStore;
    }

    public DashboardResponse getDashboard() {
        DashboardResponse dashboard = new DashboardResponse();
        dashboard.setRetentionRate(analyticsRepository.getRetentionRate());
        dashboard.setWorkoutFrequency(analyticsRepository.getWorkoutFrequency());
        dashboard.setRevenue(analyticsRepository.getRevenue());
        dashboard.setGymOccupancy(analyticsRepository.getGymOccupancy());
        dashboard.setAdditionalMetrics(new ArrayList<>());
        dashboard.setGeneratedAt(Instant.now());
        return dashboard;
    }

    public List<ReportResponse> getReports(String type) {
        if (type == null || type.isEmpty()) {
            type = "weekly";
        }
        return analyticsRepository.getReports(type);
    }

    public MetricsResponse getRealTimeMetrics() {
        MetricsResponse metrics = new MetricsResponse();
        metrics.setActiveUsers(metricsStore.getActiveUsers());
        metrics.setActiveWorkouts(metricsStore.getActiveWorkouts());
        metrics.setEventsPerSecond(metricsStore.getEventsPerSecond());
        metrics.setTotalEventsProcessed(metricsStore.getTotalEventsProcessed());
        metrics.setBookingsToday(metricsStore.getBookingsToday());
        metrics.setNutritionLogsToday(metricsStore.getNutritionLogsToday());
        metrics.setTimestamp(Instant.now());
        return metrics;
    }

    public void saveReport(ReportResponse report) {
        analyticsRepository.saveReport(report);
    }
}
