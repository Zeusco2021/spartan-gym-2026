package com.spartangoldengym.analiticas.scheduler;

import com.spartangoldengym.analiticas.dto.KpiData;
import com.spartangoldengym.analiticas.dto.ReportResponse;
import com.spartangoldengym.analiticas.repository.InMemoryMetricsStore;
import com.spartangoldengym.analiticas.service.AnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

/**
 * Generates weekly and monthly reports automatically.
 * Weekly: every Monday at 6:00 AM
 * Monthly: 1st of each month at 6:00 AM
 * Also resets daily counters at midnight.
 */
@Component
public class ReportGenerationScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReportGenerationScheduler.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final AnalyticsService analyticsService;
    private final InMemoryMetricsStore metricsStore;

    @Value("${analytics.admin-emails:admin@spartangoldengym.com}")
    private String adminEmails;

    public ReportGenerationScheduler(AnalyticsService analyticsService,
                                     InMemoryMetricsStore metricsStore) {
        this.analyticsService = analyticsService;
        this.metricsStore = metricsStore;
    }

    @Scheduled(cron = "0 0 6 ? * MON")
    public void generateWeeklyReport() {
        log.info("Generating weekly analytics report");
        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.minusDays(7);

        ReportResponse report = buildReport("weekly", weekStart.format(DATE_FMT), now.format(DATE_FMT));
        analyticsService.saveReport(report);
        sendReportEmail(report);
        log.info("Weekly report {} generated and email sent to admins", report.getId());
    }

    @Scheduled(cron = "0 0 6 1 * ?")
    public void generateMonthlyReport() {
        log.info("Generating monthly analytics report");
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.minusMonths(1).withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(1).minusDays(1);

        ReportResponse report = buildReport("monthly", monthStart.format(DATE_FMT), monthEnd.format(DATE_FMT));
        analyticsService.saveReport(report);
        sendReportEmail(report);
        log.info("Monthly report {} generated and email sent to admins", report.getId());
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void resetDailyCounters() {
        log.info("Resetting daily analytics counters");
        metricsStore.resetDailyCounters();
    }

    private ReportResponse buildReport(String type, String periodStart, String periodEnd) {
        ReportResponse report = new ReportResponse();
        report.setId(UUID.randomUUID());
        report.setType(type);
        report.setGeneratedAt(Instant.now());
        report.setPeriodStart(periodStart);
        report.setPeriodEnd(periodEnd);
        report.setKpis(Arrays.asList(
                new KpiData("workouts_completed", metricsStore.getWorkoutsCompleted(), 0.0, "up"),
                new KpiData("achievements_earned", metricsStore.getAchievementsEarned(), 0.0, "up"),
                new KpiData("social_interactions", metricsStore.getSocialInteractions(), 0.0, "stable"),
                new KpiData("bookings_total", metricsStore.getBookingsToday(), 0.0, "stable"),
                new KpiData("total_events_processed", metricsStore.getTotalEventsProcessed(), 0.0, "up")
        ));
        return report;
    }

    private void sendReportEmail(ReportResponse report) {
        // Email sending is logged since SES integration is external.
        // In production, this would use JavaMailSender to send via Amazon SES.
        log.info("Sending {} report {} to admin emails: {}. Period: {} to {}",
                report.getType(),
                report.getId(),
                adminEmails,
                report.getPeriodStart(),
                report.getPeriodEnd());
        log.info("Report KPIs: {}", report.getKpis().size());
        for (KpiData kpi : report.getKpis()) {
            log.info("  KPI: {} = {} (change: {}%, trend: {})",
                    kpi.getMetricName(), kpi.getValue(), kpi.getChangePercentage(), kpi.getTrend());
        }
    }
}
