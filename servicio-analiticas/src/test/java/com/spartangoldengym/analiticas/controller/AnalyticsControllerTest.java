package com.spartangoldengym.analiticas.controller;

import com.spartangoldengym.analiticas.dto.DashboardResponse;
import com.spartangoldengym.analiticas.dto.KpiData;
import com.spartangoldengym.analiticas.dto.MetricsResponse;
import com.spartangoldengym.analiticas.dto.RealTimeMetricsResponse;
import com.spartangoldengym.analiticas.dto.ReportResponse;
import com.spartangoldengym.analiticas.service.AnalyticsService;
import com.spartangoldengym.analiticas.service.RealTimeAggregationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnalyticsControllerTest {

    private AnalyticsService analyticsService;
    private RealTimeAggregationService realTimeAggregationService;
    private AnalyticsController controller;

    @BeforeEach
    void setUp() {
        analyticsService = mock(AnalyticsService.class);
        realTimeAggregationService = mock(RealTimeAggregationService.class);
        controller = new AnalyticsController(analyticsService, realTimeAggregationService);
    }

    @Test
    void getDashboard_returnsOkWithDashboard() {
        DashboardResponse dashboard = new DashboardResponse();
        dashboard.setRetentionRate(new KpiData("retention_rate", 85.0, 2.5, "up"));
        dashboard.setGeneratedAt(Instant.now());
        when(analyticsService.getDashboard()).thenReturn(dashboard);

        ResponseEntity<DashboardResponse> response = controller.getDashboard();

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(85.0, response.getBody().getRetentionRate().getValue());
    }

    @Test
    void getReports_returnsOkWithReportsList() {
        ReportResponse report = new ReportResponse();
        report.setId(UUID.randomUUID());
        report.setType("weekly");
        when(analyticsService.getReports("weekly")).thenReturn(Arrays.asList(report));

        ResponseEntity<List<ReportResponse>> response = controller.getReports("weekly");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getMetrics_returnsOkWithMetrics() {
        MetricsResponse metrics = new MetricsResponse();
        metrics.setActiveUsers(10);
        metrics.setActiveWorkouts(5);
        metrics.setTimestamp(Instant.now());
        when(analyticsService.getRealTimeMetrics()).thenReturn(metrics);

        ResponseEntity<MetricsResponse> response = controller.getMetrics();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(10, response.getBody().getActiveUsers());
        assertEquals(5, response.getBody().getActiveWorkouts());
    }

    @Test
    void getRealTimeAggregations_returnsOkWithAggregations() {
        RealTimeMetricsResponse metricsResponse = new RealTimeMetricsResponse();
        metricsResponse.setWorkoutsByUser(new HashMap<>());
        metricsResponse.setPerformanceByUser(new HashMap<>());
        metricsResponse.setEngagementByUser(new HashMap<>());
        metricsResponse.setTimestamp(Instant.now());
        when(realTimeAggregationService.getRealTimeAggregations()).thenReturn(metricsResponse);

        ResponseEntity<RealTimeMetricsResponse> response = controller.getRealTimeAggregations();

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getWorkoutsByUser());
        assertNotNull(response.getBody().getPerformanceByUser());
        assertNotNull(response.getBody().getEngagementByUser());
    }
}
