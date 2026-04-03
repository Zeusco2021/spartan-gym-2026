package com.spartangoldengym.analiticas.consumer;

import com.spartangoldengym.analiticas.repository.InMemoryMetricsStore;
import com.spartangoldengym.common.config.KafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer that aggregates events from multiple topics
 * for real-time analytics metrics.
 */
@Component
public class AnalyticsEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsEventConsumer.class);

    private final InMemoryMetricsStore metricsStore;

    public AnalyticsEventConsumer(InMemoryMetricsStore metricsStore) {
        this.metricsStore = metricsStore;
    }

    @KafkaListener(topics = KafkaTopics.WORKOUT_COMPLETED, groupId = "analytics-group")
    public void onWorkoutCompleted(String event) {
        log.debug("Received workout.completed event: {}", event);
        metricsStore.incrementWorkoutsCompleted();
        metricsStore.decrementActiveWorkouts();
        metricsStore.incrementTotalEvents();
        metricsStore.incrementTopicCount(KafkaTopics.WORKOUT_COMPLETED);
    }

    @KafkaListener(topics = KafkaTopics.USER_ACHIEVEMENTS, groupId = "analytics-group")
    public void onUserAchievement(String event) {
        log.debug("Received user.achievements event: {}", event);
        metricsStore.incrementAchievementsEarned();
        metricsStore.incrementTotalEvents();
        metricsStore.incrementTopicCount(KafkaTopics.USER_ACHIEVEMENTS);
    }

    @KafkaListener(topics = KafkaTopics.SOCIAL_INTERACTIONS, groupId = "analytics-group")
    public void onSocialInteraction(String event) {
        log.debug("Received social.interactions event: {}", event);
        metricsStore.incrementSocialInteractions();
        metricsStore.incrementTotalEvents();
        metricsStore.incrementTopicCount(KafkaTopics.SOCIAL_INTERACTIONS);
    }

    @KafkaListener(topics = KafkaTopics.NUTRITION_LOGS, groupId = "analytics-group")
    public void onNutritionLog(String event) {
        log.debug("Received nutrition.logs event: {}", event);
        metricsStore.incrementNutritionLogsToday();
        metricsStore.incrementTotalEvents();
        metricsStore.incrementTopicCount(KafkaTopics.NUTRITION_LOGS);
    }

    @KafkaListener(topics = KafkaTopics.GYM_OCCUPANCY, groupId = "analytics-group")
    public void onGymOccupancy(String event) {
        log.debug("Received gym.occupancy event: {}", event);
        metricsStore.incrementTotalEvents();
        metricsStore.incrementTopicCount(KafkaTopics.GYM_OCCUPANCY);
    }

    @KafkaListener(topics = KafkaTopics.BOOKINGS_EVENTS, groupId = "analytics-group")
    public void onBookingEvent(String event) {
        log.debug("Received bookings.events event: {}", event);
        metricsStore.incrementBookingsToday();
        metricsStore.incrementTotalEvents();
        metricsStore.incrementTopicCount(KafkaTopics.BOOKINGS_EVENTS);
    }
}
