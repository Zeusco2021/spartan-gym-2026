package com.spartangoldengym.analiticas.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryMetricsStoreTest {

    private InMemoryMetricsStore store;

    @BeforeEach
    void setUp() {
        store = new InMemoryMetricsStore();
    }

    @Test
    void activeUsers_incrementAndDecrement() {
        store.incrementActiveUsers();
        store.incrementActiveUsers();
        assertEquals(2, store.getActiveUsers());

        store.decrementActiveUsers();
        assertEquals(1, store.getActiveUsers());
    }

    @Test
    void activeWorkouts_incrementAndDecrement() {
        store.incrementActiveWorkouts();
        assertEquals(1, store.getActiveWorkouts());

        store.decrementActiveWorkouts();
        assertEquals(0, store.getActiveWorkouts());
    }

    @Test
    void totalEventsProcessed_increments() {
        store.incrementTotalEvents();
        store.incrementTotalEvents();
        store.incrementTotalEvents();
        assertEquals(3, store.getTotalEventsProcessed());
    }

    @Test
    void bookingsToday_incrementsAndResets() {
        store.incrementBookingsToday();
        store.incrementBookingsToday();
        assertEquals(2, store.getBookingsToday());

        store.resetDailyCounters();
        assertEquals(0, store.getBookingsToday());
    }

    @Test
    void nutritionLogsToday_incrementsAndResets() {
        store.incrementNutritionLogsToday();
        assertEquals(1, store.getNutritionLogsToday());

        store.resetDailyCounters();
        assertEquals(0, store.getNutritionLogsToday());
    }

    @Test
    void topicCount_tracksPerTopic() {
        store.incrementTopicCount("workout.completed");
        store.incrementTopicCount("workout.completed");
        store.incrementTopicCount("nutrition.logs");

        assertEquals(2, store.getTopicCount("workout.completed"));
        assertEquals(1, store.getTopicCount("nutrition.logs"));
        assertEquals(0, store.getTopicCount("nonexistent.topic"));
    }

    @Test
    void eventsPerSecond_returnsNonNegative() {
        store.incrementTotalEvents();
        store.incrementTotalEvents();
        double eps = store.getEventsPerSecond();
        assertTrue(eps >= 0.0);
    }

    @Test
    void workoutsCompleted_increments() {
        store.incrementWorkoutsCompleted();
        store.incrementWorkoutsCompleted();
        assertEquals(2, store.getWorkoutsCompleted());
    }

    @Test
    void achievementsEarned_increments() {
        store.incrementAchievementsEarned();
        assertEquals(1, store.getAchievementsEarned());
    }

    @Test
    void socialInteractions_increments() {
        store.incrementSocialInteractions();
        store.incrementSocialInteractions();
        store.incrementSocialInteractions();
        assertEquals(3, store.getSocialInteractions());
    }
}
