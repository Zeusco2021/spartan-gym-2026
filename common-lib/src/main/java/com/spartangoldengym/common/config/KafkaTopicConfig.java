package com.spartangoldengym.common.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.Map;

/**
 * Kafka topic auto-configuration for Amazon MSK.
 * Creates all platform topics with their specified partition counts,
 * retention policies, and corresponding Dead Letter Queue (DLQ) topics.
 *
 * Validates: Requirements 11.3, 11.4
 */
@Configuration
public class KafkaTopicConfig {

    private static final String RETENTION_MS = "retention.ms";
    private static final String RETENTION_7_DAYS = "604800000";   // 7 days in ms
    private static final String RETENTION_24_HOURS = "86400000";  // 24 hours in ms

    private static final short REPLICATION_FACTOR = 3;
    private static final int DLQ_PARTITIONS = 3;

    // --- Main topics ---

    @Bean
    public NewTopic workoutCompletedTopic() {
        return TopicBuilder.name(KafkaTopics.WORKOUT_COMPLETED)
                .partitions(20)
                .replicas(REPLICATION_FACTOR)
                .config(RETENTION_MS, RETENTION_7_DAYS)
                .build();
    }

    @Bean
    public NewTopic userAchievementsTopic() {
        return TopicBuilder.name(KafkaTopics.USER_ACHIEVEMENTS)
                .partitions(10)
                .replicas(REPLICATION_FACTOR)
                .config(RETENTION_MS, RETENTION_7_DAYS)
                .build();
    }

    @Bean
    public NewTopic realTimeHeartrateTopic() {
        return TopicBuilder.name(KafkaTopics.REAL_TIME_HEARTRATE)
                .partitions(50)
                .replicas(REPLICATION_FACTOR)
                .config(RETENTION_MS, RETENTION_24_HOURS)
                .build();
    }

    @Bean
    public NewTopic aiRecommendationsRequestTopic() {
        return TopicBuilder.name(KafkaTopics.AI_RECOMMENDATIONS_REQUEST)
                .partitions(15)
                .replicas(REPLICATION_FACTOR)
                .config(RETENTION_MS, RETENTION_7_DAYS)
                .build();
    }

    @Bean
    public NewTopic socialInteractionsTopic() {
        return TopicBuilder.name(KafkaTopics.SOCIAL_INTERACTIONS)
                .partitions(20)
                .replicas(REPLICATION_FACTOR)
                .config(RETENTION_MS, RETENTION_7_DAYS)
                .build();
    }

    @Bean
    public NewTopic nutritionLogsTopic() {
        return TopicBuilder.name(KafkaTopics.NUTRITION_LOGS)
                .partitions(20)
                .replicas(REPLICATION_FACTOR)
                .config(RETENTION_MS, RETENTION_7_DAYS)
                .build();
    }

    @Bean
    public NewTopic gymOccupancyTopic() {
        return TopicBuilder.name(KafkaTopics.GYM_OCCUPANCY)
                .partitions(10)
                .replicas(REPLICATION_FACTOR)
                .config(RETENTION_MS, RETENTION_24_HOURS)
                .build();
    }

    @Bean
    public NewTopic bookingsEventsTopic() {
        return TopicBuilder.name(KafkaTopics.BOOKINGS_EVENTS)
                .partitions(10)
                .replicas(REPLICATION_FACTOR)
                .config(RETENTION_MS, RETENTION_7_DAYS)
                .build();
    }

    // --- Dead Letter Queue topics ---

    @Bean
    public NewTopic workoutCompletedDlq() {
        return TopicBuilder.name(KafkaTopics.WORKOUT_COMPLETED_DLQ)
                .partitions(DLQ_PARTITIONS)
                .replicas(REPLICATION_FACTOR)
                .build();
    }

    @Bean
    public NewTopic userAchievementsDlq() {
        return TopicBuilder.name(KafkaTopics.USER_ACHIEVEMENTS_DLQ)
                .partitions(DLQ_PARTITIONS)
                .replicas(REPLICATION_FACTOR)
                .build();
    }

    @Bean
    public NewTopic realTimeHeartrateDlq() {
        return TopicBuilder.name(KafkaTopics.REAL_TIME_HEARTRATE_DLQ)
                .partitions(DLQ_PARTITIONS)
                .replicas(REPLICATION_FACTOR)
                .build();
    }

    @Bean
    public NewTopic aiRecommendationsRequestDlq() {
        return TopicBuilder.name(KafkaTopics.AI_RECOMMENDATIONS_REQUEST_DLQ)
                .partitions(DLQ_PARTITIONS)
                .replicas(REPLICATION_FACTOR)
                .build();
    }

    @Bean
    public NewTopic socialInteractionsDlq() {
        return TopicBuilder.name(KafkaTopics.SOCIAL_INTERACTIONS_DLQ)
                .partitions(DLQ_PARTITIONS)
                .replicas(REPLICATION_FACTOR)
                .build();
    }

    @Bean
    public NewTopic nutritionLogsDlq() {
        return TopicBuilder.name(KafkaTopics.NUTRITION_LOGS_DLQ)
                .partitions(DLQ_PARTITIONS)
                .replicas(REPLICATION_FACTOR)
                .build();
    }

    @Bean
    public NewTopic gymOccupancyDlq() {
        return TopicBuilder.name(KafkaTopics.GYM_OCCUPANCY_DLQ)
                .partitions(DLQ_PARTITIONS)
                .replicas(REPLICATION_FACTOR)
                .build();
    }

    @Bean
    public NewTopic bookingsEventsDlq() {
        return TopicBuilder.name(KafkaTopics.BOOKINGS_EVENTS_DLQ)
                .partitions(DLQ_PARTITIONS)
                .replicas(REPLICATION_FACTOR)
                .build();
    }
}
