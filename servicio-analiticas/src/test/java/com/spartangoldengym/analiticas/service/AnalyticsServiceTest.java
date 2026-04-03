package com.spartangoldengym.analiticas.service;

import com.spartangoldengym.analiticas.dto.DashboardResponse;
import com.spartangoldengym.analiticas.dto.KpiData;
import com.spartangoldengym.analiticas.dto.MetricsResponse;
import com.spartangoldengym.analiticas.dto.ReportResponse;
import com.spartangoldengym.analiticas.repository.AnalyticsRepository;
import com.spartangoldengym.analiticas.repository.InMemoryMetricsStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnalyticsServiceTest {

    private AnalyticsRepository analyticsRepository;
    private InMemoryMetricsStore metricsStore;
    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsRepository = mock(AnalyticsRepository.class);
        metricsStore = new InMemoryMetricsStore();
        analyticsService = new AnalyticsService(analyticsRepository, metricsStore);
    }

    @Test
    void getDashboard_returnsAllKpis() {
        when(analyticsRepository.getRetentionRate())
                .thenReturn(new KpiData("retention_rate", 85.0, 2.5, "up"));
        when(analyticsRepository.getWorkoutFrequency())
                .thenReturn(new KpiData("workout_frequency", 3.2, -0.1, "stable"));
        when(analyticsRepository.getRevenue())
                .thenReturn(new KpiData("revenue", 50000.0, 5.0, "up"));
        when(analyticsRepository.getGymOccupancy())
                .thenReturn(new KpiData("gym_occupancy", 72.0, 1.0, "up"));

        DashboardResponse dashboard = analyticsService.getDashboard();

        assertNotNull(dashboard);
        assertEquals("retention_rate", dashboard.getRetentionRate().getMetricName());
        assertEquals(85.0, dashboard.getRetentionRate().getValue());
        assertEquals("workout_frequency", dashboard.getWorkoutFrequency().getMetricName());
        assertEquals("revenue", dashboard.getRevenue().getMetricName());
        assertEquals("gym_occupancy", dashboard.getGymOccupancy().getMetricName());
        assertNotNull(dashboard.getGeneratedAt());
    }

    @Test
    void getReports_withType_delegatesToRepository() {
        ReportResponse report = new ReportResponse();
        report.setId(UUID.randomUUID());
        report.setType("weekly");
        report.setGeneratedAt(Instant.now());
        when(analyticsRepository.getReports("weekly")).thenReturn(Arrays.asList(report));

        List<ReportResponse> reports = analyticsService.getReports("weekly");

        assertEquals(1, reports.size());
        assertEquals("weekly", reports.get(0).getType());
        verify(analyticsRepository).getReports("weekly");
    }

    @Test
    void getReports_nullType_defaultsToWeekly() {
        when(analyticsRepository.getReports("weekly")).thenReturn(Collections.emptyList());

        List<ReportResponse> reports = analyticsService.getReports(null);

        verify(analyticsRepository).getReports("weekly");
    }

    @Test
    void getReports_emptyType_defaultsToWeekly() {
        when(analyticsRepository.getReports("weekly")).thenReturn(Collections.emptyList());

        List<ReportResponse> reports = analyticsService.getReports("");

        verify(analyticsRepository).getReports("weekly");
    }

    @Test
    void getRealTimeMetrics_returnsInMemoryCounters() {
        metricsStore.incrementActiveUsers();
        metricsStore.incrementActiveUsers();
        metricsStore.incrementActiveWorkouts();
        metricsStore.incrementBookingsToday();
        metricsStore.incrementBookingsToday();
        metricsStore.incrementBookingsToday();
        metricsStore.incrementNutritionLogsToday();

        MetricsResponse metrics = analyticsService.getRealTimeMetrics();

        assertEquals(2, metrics.getActiveUsers());
        assertEquals(1, metrics.getActiveWorkouts());
        assertEquals(3, metrics.getBookingsToday());
        assertEquals(1, metrics.getNutritionLogsToday());
        assertNotNull(metrics.getTimestamp());
    }

    @Test
    void saveReport_delegatesToRepository() {
        ReportResponse report = new ReportResponse();
        report.setId(UUID.randomUUID());
        report.setType("monthly");

        analyticsService.saveReport(report);

        verify(analyticsRepository).saveReport(report);
    }
}
