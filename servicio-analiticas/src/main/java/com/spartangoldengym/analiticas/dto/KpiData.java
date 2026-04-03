package com.spartangoldengym.analiticas.dto;

public class KpiData {

    private String metricName;
    private double value;
    private double changePercentage;
    private String trend;

    public KpiData() {
    }

    public KpiData(String metricName, double value, double changePercentage, String trend) {
        this.metricName = metricName;
        this.value = value;
        this.changePercentage = changePercentage;
        this.trend = trend;
    }

    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
    public double getChangePercentage() { return changePercentage; }
    public void setChangePercentage(double changePercentage) { this.changePercentage = changePercentage; }
    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }
}
