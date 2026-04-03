package com.spartangoldengym.analiticas.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ReportResponse {

    private UUID id;
    private String type;
    private List<KpiData> kpis;
    private Instant generatedAt;
    private String periodStart;
    private String periodEnd;

    public ReportResponse() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public List<KpiData> getKpis() { return kpis; }
    public void setKpis(List<KpiData> kpis) { this.kpis = kpis; }
    public Instant getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Instant generatedAt) { this.generatedAt = generatedAt; }
    public String getPeriodStart() { return periodStart; }
    public void setPeriodStart(String periodStart) { this.periodStart = periodStart; }
    public String getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(String periodEnd) { this.periodEnd = periodEnd; }
}
