package com.spartangoldengym.analiticas.controller;

import com.spartangoldengym.analiticas.dto.DashboardResponse;
import com.spartangoldengym.analiticas.dto.MetricsResponse;
import com.spartangoldengym.analiticas.dto.RealTimeMetricsResponse;
import com.spartangoldengym.analiticas.dto.ReportResponse;
import com.spartangoldengym.analiticas.service.AnalyticsService;
import com.spartangoldengym.analiticas.service.RealTimeAggregationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final RealTimeAggregationService realTimeAggregationService;

    public AnalyticsController(AnalyticsService analyticsService,
                               RealTimeAggregationService realTimeAggregationService) {
        this.analyticsService = analyticsService;
        this.realTimeAggregationService = realTimeAggregationService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        DashboardResponse dashboard = analyticsService.getDashboard();
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/reports")
    public ResponseEntity<List<ReportResponse>> getReports(
            @RequestParam(required = false, defaultValue = "weekly") String type) {
        List<ReportResponse> reports = analyticsService.getReports(type);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/metrics")
    public ResponseEntity<MetricsResponse> getMetrics() {
        MetricsResponse metrics = analyticsService.getRealTimeMetrics();
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/metrics/realtime")
    public ResponseEntity<RealTimeMetricsResponse> getRealTimeAggregations() {
        RealTimeMetricsResponse metrics = realTimeAggregationService.getRealTimeAggregations();
        return ResponseEntity.ok(metrics);
    }
}
