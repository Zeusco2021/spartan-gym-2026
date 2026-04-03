package com.spartangoldengym.analiticas.dto;

import java.time.Instant;

public class MetricsResponse {

    private long activeUsers;
    private long activeWorkouts;
    private double eventsPerSecond;
    private long totalEventsProcessed;
    private long bookingsToday;
    private long nutritionLogsToday;
    private Instant timestamp;

    public MetricsResponse() {
    }

    public long getActiveUsers() { return activeUsers; }
    public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }
    public long getActiveWorkouts() { return activeWorkouts; }
    public void setActiveWorkouts(long activeWorkouts) { this.activeWorkouts = activeWorkouts; }
    public double getEventsPerSecond() { return eventsPerSecond; }
    public void setEventsPerSecond(double eventsPerSecond) { this.eventsPerSecond = eventsPerSecond; }
    public long getTotalEventsProcessed() { return totalEventsProcessed; }
    public void setTotalEventsProcessed(long totalEventsProcessed) { this.totalEventsProcessed = totalEventsProcessed; }
    public long getBookingsToday() { return bookingsToday; }
    public void setBookingsToday(long bookingsToday) { this.bookingsToday = bookingsToday; }
    public long getNutritionLogsToday() { return nutritionLogsToday; }
    public void setNutritionLogsToday(long nutritionLogsToday) { this.nutritionLogsToday = nutritionLogsToday; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
