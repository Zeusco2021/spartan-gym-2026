package com.spartangoldengym.analiticas.consumer;

import com.spartangoldengym.analiticas.repository.InMemoryMetricsStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnalyticsEventConsumerTest {

    private InMemoryMetricsStore metricsStore;
    private AnalyticsEventConsumer consumer;

    @BeforeEach
    void setUp() {
        metricsStore = new InMemoryMetricsStore();
        consumer = new AnalyticsEventConsumer(metricsStore);
    }

    @Test
    void onWorkoutCompleted_incrementsCounters() {
        consumer.onWorkoutCompleted("{\"userId\":\"abc\",\"event\":\"completed\"}");

        assertEquals(1, metricsStore.getWorkoutsCompleted());
        assertEquals(1, metricsStore.getTotalEventsProcessed());
    }

    @Test
    void onUserAchievement_incrementsCounters() {
        consumer.onUserAchievement("{\"userId\":\"abc\",\"achievement\":\"first_workout\"}");

        assertEquals(1, metricsStore.getAchievementsEarned());
        assertEquals(1, metricsStore.getTotalEventsProcessed());
    }

    @Test
    void onSocialInteraction_incrementsCounters() {
        consumer.onSocialInteraction("{\"userId\":\"abc\",\"type\":\"like\"}");

        assertEquals(1, metricsStore.getSocialInteractions());
        assertEquals(1, metricsStore.getTotalEventsProcessed());
    }

    @Test
    void onNutritionLog_incrementsCounters() {
        consumer.onNutritionLog("{\"userId\":\"abc\",\"food\":\"chicken\"}");

        assertEquals(1, metricsStore.getNutritionLogsToday());
        assertEquals(1, metricsStore.getTotalEventsProcessed());
    }

    @Test
    void onGymOccupancy_incrementsTotalEvents() {
        consumer.onGymOccupancy("{\"gymId\":\"gym1\",\"occupancy\":45}");

        assertEquals(1, metricsStore.getTotalEventsProcessed());
    }

    @Test
    void onBookingEvent_incrementsCounters() {
        consumer.onBookingEvent("{\"classId\":\"class1\",\"event\":\"reserved\"}");

        assertEquals(1, metricsStore.getBookingsToday());
        assertEquals(1, metricsStore.getTotalEventsProcessed());
    }

    @Test
    void multipleEvents_accumulateCorrectly() {
        consumer.onWorkoutCompleted("{}");
        consumer.onWorkoutCompleted("{}");
        consumer.onBookingEvent("{}");
        consumer.onNutritionLog("{}");
        consumer.onNutritionLog("{}");
        consumer.onNutritionLog("{}");

        assertEquals(2, metricsStore.getWorkoutsCompleted());
        assertEquals(1, metricsStore.getBookingsToday());
        assertEquals(3, metricsStore.getNutritionLogsToday());
        assertEquals(6, metricsStore.getTotalEventsProcessed());
    }
}
