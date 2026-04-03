package com.spartangoldengym.analiticas.repository;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory aggregation store for real-time metrics from Kafka events.
 * Uses atomic counters for thread-safe concurrent updates.
 */
@Component
public class InMemoryMetricsStore {

    private final AtomicLong activeUsers = new AtomicLong(0);
    private final AtomicLong activeWorkouts = new AtomicLong(0);
    private final AtomicLong totalEventsProcessed = new AtomicLong(0);
    private final AtomicLong bookingsToday = new AtomicLong(0);
    private final AtomicLong nutritionLogsToday = new AtomicLong(0);
    private final AtomicLong workoutsCompleted = new AtomicLong(0);
    private final AtomicLong achievementsEarned = new AtomicLong(0);
    private final AtomicLong socialInteractions = new AtomicLong(0);

    private final ConcurrentHashMap<String, AtomicLong> eventCountsByTopic = new ConcurrentHashMap<String, AtomicLong>();

    // Sliding window for events-per-second calculation
    private final AtomicLong eventsInLastMinute = new AtomicLong(0);
    private volatile long lastMinuteResetTime = System.currentTimeMillis();

    public void incrementActiveUsers() { activeUsers.incrementAndGet(); }
    public void decrementActiveUsers() { activeUsers.decrementAndGet(); }
    public long getActiveUsers() { return activeUsers.get(); }

    public void incrementActiveWorkouts() { activeWorkouts.incrementAndGet(); }
    public void decrementActiveWorkouts() { activeWorkouts.decrementAndGet(); }
    public long getActiveWorkouts() { return activeWorkouts.get(); }

    public void incrementTotalEvents() {
        totalEventsProcessed.incrementAndGet();
        eventsInLastMinute.incrementAndGet();
    }

    public long getTotalEventsProcessed() { return totalEventsProcessed.get(); }

    public void incrementBookingsToday() { bookingsToday.incrementAndGet(); }
    public long getBookingsToday() { return bookingsToday.get(); }

    public void incrementNutritionLogsToday() { nutritionLogsToday.incrementAndGet(); }
    public long getNutritionLogsToday() { return nutritionLogsToday.get(); }

    public void incrementWorkoutsCompleted() { workoutsCompleted.incrementAndGet(); }
    public long getWorkoutsCompleted() { return workoutsCompleted.get(); }

    public void incrementAchievementsEarned() { achievementsEarned.incrementAndGet(); }
    public long getAchievementsEarned() { return achievementsEarned.get(); }

    public void incrementSocialInteractions() { socialInteractions.incrementAndGet(); }
    public long getSocialInteractions() { return socialInteractions.get(); }

    public void incrementTopicCount(String topic) {
        eventCountsByTopic.computeIfAbsent(topic, k -> new AtomicLong(0)).incrementAndGet();
    }

    public long getTopicCount(String topic) {
        AtomicLong count = eventCountsByTopic.get(topic);
        return count != null ? count.get() : 0;
    }

    public double getEventsPerSecond() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastMinuteResetTime;
        if (elapsed >= 60000) {
            long events = eventsInLastMinute.getAndSet(0);
            lastMinuteResetTime = now;
            return events / 60.0;
        }
        double seconds = elapsed / 1000.0;
        return seconds > 0 ? eventsInLastMinute.get() / seconds : 0.0;
    }

    public void resetDailyCounters() {
        bookingsToday.set(0);
        nutritionLogsToday.set(0);
    }
}
