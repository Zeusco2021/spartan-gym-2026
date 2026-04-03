package com.spartangoldengym.analiticas.repository;

import com.spartangoldengym.analiticas.dto.KpiData;
import com.spartangoldengym.analiticas.dto.ReportResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Repository
public class RedshiftAnalyticsRepository implements AnalyticsRepository {

    private static final Logger log = LoggerFactory.getLogger(RedshiftAnalyticsRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public RedshiftAnalyticsRepository(JdbcTemplate redshiftJdbcTemplate) {
        this.jdbcTemplate = redshiftJdbcTemplate;
    }

    @Override
    public KpiData getRetentionRate() {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT metric_name, value, change_percentage, trend FROM kpi_snapshots " +
                            "WHERE metric_name = 'retention_rate' ORDER BY created_at DESC LIMIT 1",
                    (rs, rowNum) -> mapKpiData(rs));
        } catch (Exception e) {
            log.warn("Could not fetch retention rate from Redshift: {}", e.getMessage());
            return new KpiData("retention_rate", 0.0, 0.0, "stable");
        }
    }

    @Override
    public KpiData getWorkoutFrequency() {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT metric_name, value, change_percentage, trend FROM kpi_snapshots " +
                            "WHERE metric_name = 'workout_frequency' ORDER BY created_at DESC LIMIT 1",
                    (rs, rowNum) -> mapKpiData(rs));
        } catch (Exception e) {
            log.warn("Could not fetch workout frequency from Redshift: {}", e.getMessage());
            return new KpiData("workout_frequency", 0.0, 0.0, "stable");
        }
    }

    @Override
    public KpiData getRevenue() {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT metric_name, value, change_percentage, trend FROM kpi_snapshots " +
                            "WHERE metric_name = 'revenue' ORDER BY created_at DESC LIMIT 1",
                    (rs, rowNum) -> mapKpiData(rs));
        } catch (Exception e) {
            log.warn("Could not fetch revenue from Redshift: {}", e.getMessage());
            return new KpiData("revenue", 0.0, 0.0, "stable");
        }
    }

    @Override
    public KpiData getGymOccupancy() {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT metric_name, value, change_percentage, trend FROM kpi_snapshots " +
                            "WHERE metric_name = 'gym_occupancy' ORDER BY created_at DESC LIMIT 1",
                    (rs, rowNum) -> mapKpiData(rs));
        } catch (Exception e) {
            log.warn("Could not fetch gym occupancy from Redshift: {}", e.getMessage());
            return new KpiData("gym_occupancy", 0.0, 0.0, "stable");
        }
    }

    @Override
    public List<ReportResponse> getReports(String type) {
        try {
            return jdbcTemplate.query(
                    "SELECT id, type, generated_at, period_start, period_end FROM analytics_reports " +
                            "WHERE type = ? ORDER BY generated_at DESC LIMIT 50",
                    (rs, rowNum) -> {
                        ReportResponse report = new ReportResponse();
                        report.setId(UUID.fromString(rs.getString("id")));
                        report.setType(rs.getString("type"));
                        Timestamp ts = rs.getTimestamp("generated_at");
                        if (ts != null) {
                            report.setGeneratedAt(ts.toInstant());
                        }
                        report.setPeriodStart(rs.getString("period_start"));
                        report.setPeriodEnd(rs.getString("period_end"));
                        report.setKpis(new ArrayList<KpiData>());
                        return report;
                    },
                    type);
        } catch (Exception e) {
            log.warn("Could not fetch reports from Redshift: {}", e.getMessage());
            return new ArrayList<ReportResponse>();
        }
    }

    @Override
    public void saveReport(ReportResponse report) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO analytics_reports (id, type, generated_at, period_start, period_end) " +
                            "VALUES (?, ?, ?, ?, ?)",
                    report.getId().toString(),
                    report.getType(),
                    Timestamp.from(report.getGeneratedAt()),
                    report.getPeriodStart(),
                    report.getPeriodEnd());
            log.info("Saved report {} of type {} to Redshift", report.getId(), report.getType());
        } catch (Exception e) {
            log.error("Failed to save report to Redshift: {}", e.getMessage());
        }
    }

    @Override
    public void saveKpiSnapshot(String metricName, double value, double changePercentage, String trend) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO kpi_snapshots (id, metric_name, value, change_percentage, trend, created_at) " +
                            "VALUES (?, ?, ?, ?, ?, ?)",
                    UUID.randomUUID().toString(),
                    metricName,
                    value,
                    changePercentage,
                    trend,
                    Timestamp.from(Instant.now()));
        } catch (Exception e) {
            log.error("Failed to save KPI snapshot to Redshift: {}", e.getMessage());
        }
    }

    private KpiData mapKpiData(ResultSet rs) throws SQLException {
        return new KpiData(
                rs.getString("metric_name"),
                rs.getDouble("value"),
                rs.getDouble("change_percentage"),
                rs.getString("trend"));
    }
}
