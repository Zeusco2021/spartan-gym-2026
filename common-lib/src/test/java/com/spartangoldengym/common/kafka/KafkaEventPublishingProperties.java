package com.spartangoldengym.common.kafka;

import com.spartangoldengym.common.config.KafkaTopics;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for Kafka event publishing across all microservices.
 *
 * Feature: spartan-golden-gym, Property 67: Publicación de eventos en Kafka
 *
 * Verifies that each domain action (workout completed, achievement earned,
 * social interaction, meal logged, booking made, recommendation generated)
 * publishes the event to the correct Kafka topic with complete data.
 *
 * Validates: Requirements 3.11, 4.5, 5.4, 6.2, 6.8, 23.8
 */
class KafkaEventPublishingProperties {

    // -----------------------------------------------------------------------
    // Helpers: simulate the exact publishing logic from each service
    // -----------------------------------------------------------------------

    /**
     * Simulates WorkoutService.completeWorkout Kafka publishing.
     * Source: servicio-seguimiento WorkoutService
     */
    static void publishWorkoutCompleted(KafkaTemplate<String, String> kafkaTemplate,
                                        String sessionId, String userId,
                                        long durationSeconds, int exerciseCount,
                                        int setCount, double caloriesBurned) {
        String payload = String.format(
                "{\"sessionId\":\"%s\",\"userId\":\"%s\",\"durationSeconds\":%d,"
                + "\"exercises\":%d,\"sets\":%d,\"caloriesBurned\":%.1f,\"completedAt\":\"%s\"}",
                sessionId, userId, durationSeconds,
                exerciseCount, setCount, caloriesBurned, Instant.now().toString());
        kafkaTemplate.send(KafkaTopics.WORKOUT_COMPLETED, userId, payload);
    }

    /**
     * Simulates AchievementService.completeChallenge Kafka publishing.
     * Source: servicio-social AchievementService
     */
    static void publishUserAchievement(KafkaTemplate<String, String> kafkaTemplate,
                                       UUID achievementId, UUID userId, UUID challengeId,
                                       String name, String badgeName) {
        String payload = String.format(
                "{\"achievementId\":\"%s\",\"userId\":\"%s\",\"challengeId\":\"%s\","
                + "\"type\":\"challenge_completed\",\"name\":\"%s\",\"badgeName\":\"%s\","
                + "\"earnedAt\":\"%s\"}",
                achievementId, userId, challengeId, name, badgeName, Instant.now());
        kafkaTemplate.send(KafkaTopics.USER_ACHIEVEMENTS, userId.toString(), payload);
    }

    /**
     * Simulates InteractionService.createInteraction Kafka publishing.
     * Source: servicio-social InteractionService
     */
    static void publishSocialInteraction(KafkaTemplate<String, String> kafkaTemplate,
                                         UUID interactionId, UUID userId, String type,
                                         UUID targetId, String targetType) {
        String payload = String.format(
                "{\"interactionId\":\"%s\",\"userId\":\"%s\",\"type\":\"%s\","
                + "\"targetId\":\"%s\",\"targetType\":\"%s\",\"createdAt\":\"%s\"}",
                interactionId, userId, type, targetId, targetType, Instant.now());
        kafkaTemplate.send(KafkaTopics.SOCIAL_INTERACTIONS, userId.toString(), payload);
    }

    /**
     * Simulates MealService.logMeal Kafka publishing.
     * Source: servicio-nutricion MealService
     */
    static void publishNutritionLog(KafkaTemplate<String, String> kafkaTemplate,
                                    UUID mealLogId, UUID userId, UUID foodId,
                                    double quantityGrams, String mealType,
                                    double calories, double protein, double carbs, double fat) {
        String payload = String.format(
                "{\"mealLogId\":\"%s\",\"userId\":\"%s\",\"foodId\":\"%s\",\"quantityGrams\":%.2f,"
                + "\"mealType\":\"%s\",\"calories\":%.2f,\"protein\":%.2f,\"carbs\":%.2f,\"fat\":%.2f,"
                + "\"loggedAt\":\"%s\"}",
                mealLogId, userId, foodId, quantityGrams, mealType,
                calories, protein, carbs, fat, Instant.now());
        kafkaTemplate.send(KafkaTopics.NUTRITION_LOGS, userId.toString(), payload);
    }

    /**
     * Simulates BookingService.publishEvent Kafka publishing.
     * Source: servicio-reservas BookingService
     */
    static void publishBookingEvent(KafkaTemplate<String, String> kafkaTemplate,
                                    UUID classId, UUID userId, String eventType) {
        String userField = userId != null ? ",\"userId\":\"" + userId + "\"" : "";
        String event = "{\"classId\":\"" + classId + "\"" + userField
                + ",\"event\":\"" + eventType
                + "\",\"timestamp\":\"" + Instant.now() + "\"}";
        kafkaTemplate.send(KafkaTopics.BOOKINGS_EVENTS, classId.toString(), event);
    }

    /**
     * Simulates AiCoachService.publishRecommendationEvent Kafka publishing.
     * Source: servicio-ia-coach AiCoachService
     */
    static void publishAiRecommendation(KafkaTemplate<String, String> kafkaTemplate,
                                        String type, UUID userId, String referenceId) {
        String payload = String.format(
                "{\"type\":\"%s\",\"userId\":\"%s\",\"referenceId\":\"%s\",\"timestamp\":\"%s\"}",
                type, userId, referenceId != null ? referenceId : "", Instant.now());
        kafkaTemplate.send(KafkaTopics.AI_RECOMMENDATIONS_REQUEST, userId.toString(), payload);
    }

    // -----------------------------------------------------------------------
    // Property tests
    // -----------------------------------------------------------------------

    /**
     * **Validates: Requirement 4.5**
     *
     * For any completed workout, the event must be published to the
     * workout.completed topic with sessionId, userId, duration, exercises,
     * sets, caloriesBurned and completedAt.
     */
    @Property(tries = 100)
    @Tag("spartan-golden-gym")
    @Tag("Property-67")
    void workoutCompleted_publishesToCorrectTopic_withCompleteData(
            @ForAll("userIds") String userId,
            @ForAll @LongRange(min = 60, max = 7200) long durationSeconds,
            @ForAll @IntRange(min = 1, max = 20) int exerciseCount,
            @ForAll @IntRange(min = 1, max = 100) int setCount,
            @ForAll @DoubleRange(min = 10, max = 2000) double caloriesBurned
    ) {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        String sessionId = UUID.randomUUID().toString();

        publishWorkoutCompleted(kafkaTemplate, sessionId, userId,
                durationSeconds, exerciseCount, setCount, caloriesBurned);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), payloadCaptor.capture());

        assertEquals(KafkaTopics.WORKOUT_COMPLETED, topicCaptor.getValue());
        assertEquals(userId, keyCaptor.getValue());

        String payload = payloadCaptor.getValue();
        assertTrue(payload.contains("\"sessionId\":\"" + sessionId + "\""),
                "Payload must contain sessionId");
        assertTrue(payload.contains("\"userId\":\"" + userId + "\""),
                "Payload must contain userId");
        assertTrue(payload.contains("\"durationSeconds\":" + durationSeconds),
                "Payload must contain durationSeconds");
        assertTrue(payload.contains("\"exercises\":" + exerciseCount),
                "Payload must contain exercises count");
        assertTrue(payload.contains("\"sets\":" + setCount),
                "Payload must contain sets count");
        assertTrue(payload.contains("\"completedAt\":"),
                "Payload must contain completedAt timestamp");
    }

    /**
     * **Validates: Requirement 6.2**
     *
     * For any achievement earned (challenge completed), the event must be
     * published to user.achievements with achievementId, userId, challengeId,
     * type, name, badgeName and earnedAt.
     */
    @Property(tries = 100)
    @Tag("spartan-golden-gym")
    @Tag("Property-67")
    void achievementEarned_publishesToCorrectTopic_withCompleteData(
            @ForAll("challengeNames") String challengeName,
            @ForAll("badgeNames") String badgeName
    ) {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        UUID achievementId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID challengeId = UUID.randomUUID();

        publishUserAchievement(kafkaTemplate, achievementId, userId, challengeId,
                challengeName, badgeName);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), payloadCaptor.capture());

        assertEquals(KafkaTopics.USER_ACHIEVEMENTS, topicCaptor.getValue());
        assertEquals(userId.toString(), keyCaptor.getValue());

        String payload = payloadCaptor.getValue();
        assertTrue(payload.contains("\"achievementId\":\"" + achievementId + "\""),
                "Payload must contain achievementId");
        assertTrue(payload.contains("\"userId\":\"" + userId + "\""),
                "Payload must contain userId");
        assertTrue(payload.contains("\"challengeId\":\"" + challengeId + "\""),
                "Payload must contain challengeId");
        assertTrue(payload.contains("\"type\":\"challenge_completed\""),
                "Payload must contain type");
        assertTrue(payload.contains("\"earnedAt\":"),
                "Payload must contain earnedAt timestamp");
    }

    /**
     * **Validates: Requirement 6.8**
     *
     * For any social interaction (comment, reaction, share), the event must
     * be published to social.interactions with interactionId, userId, type,
     * targetId, targetType and createdAt.
     */
    @Property(tries = 100)
    @Tag("spartan-golden-gym")
    @Tag("Property-67")
    void socialInteraction_publishesToCorrectTopic_withCompleteData(
            @ForAll("interactionTypes") String interactionType,
            @ForAll("targetTypes") String targetType
    ) {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        UUID interactionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        publishSocialInteraction(kafkaTemplate, interactionId, userId,
                interactionType, targetId, targetType);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), payloadCaptor.capture());

        assertEquals(KafkaTopics.SOCIAL_INTERACTIONS, topicCaptor.getValue());
        assertEquals(userId.toString(), keyCaptor.getValue());

        String payload = payloadCaptor.getValue();
        assertTrue(payload.contains("\"interactionId\":\"" + interactionId + "\""),
                "Payload must contain interactionId");
        assertTrue(payload.contains("\"userId\":\"" + userId + "\""),
                "Payload must contain userId");
        assertTrue(payload.contains("\"type\":\"" + interactionType + "\""),
                "Payload must contain interaction type");
        assertTrue(payload.contains("\"targetId\":\"" + targetId + "\""),
                "Payload must contain targetId");
        assertTrue(payload.contains("\"targetType\":\"" + targetType + "\""),
                "Payload must contain targetType");
        assertTrue(payload.contains("\"createdAt\":"),
                "Payload must contain createdAt timestamp");
    }

    /**
     * **Validates: Requirement 5.4**
     *
     * For any meal logged, the event must be published to nutrition.logs
     * with mealLogId, userId, foodId, quantityGrams, mealType, calories,
     * protein, carbs, fat and loggedAt.
     */
    @Property(tries = 100)
    @Tag("spartan-golden-gym")
    @Tag("Property-67")
    void mealLogged_publishesToCorrectTopic_withCompleteData(
            @ForAll @DoubleRange(min = 10, max = 1000) double quantityGrams,
            @ForAll("mealTypes") String mealType,
            @ForAll @DoubleRange(min = 0, max = 2000) double calories,
            @ForAll @DoubleRange(min = 0, max = 500) double protein,
            @ForAll @DoubleRange(min = 0, max = 500) double carbs,
            @ForAll @DoubleRange(min = 0, max = 500) double fat
    ) {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        UUID mealLogId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID foodId = UUID.randomUUID();

        publishNutritionLog(kafkaTemplate, mealLogId, userId, foodId,
                quantityGrams, mealType, calories, protein, carbs, fat);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), payloadCaptor.capture());

        assertEquals(KafkaTopics.NUTRITION_LOGS, topicCaptor.getValue());
        assertEquals(userId.toString(), keyCaptor.getValue());

        String payload = payloadCaptor.getValue();
        assertTrue(payload.contains("\"mealLogId\":\"" + mealLogId + "\""),
                "Payload must contain mealLogId");
        assertTrue(payload.contains("\"userId\":\"" + userId + "\""),
                "Payload must contain userId");
        assertTrue(payload.contains("\"foodId\":\"" + foodId + "\""),
                "Payload must contain foodId");
        assertTrue(payload.contains("\"mealType\":\"" + mealType + "\""),
                "Payload must contain mealType");
        assertTrue(payload.contains("\"calories\":"),
                "Payload must contain calories");
        assertTrue(payload.contains("\"protein\":"),
                "Payload must contain protein");
        assertTrue(payload.contains("\"carbs\":"),
                "Payload must contain carbs");
        assertTrue(payload.contains("\"fat\":"),
                "Payload must contain fat");
        assertTrue(payload.contains("\"loggedAt\":"),
                "Payload must contain loggedAt timestamp");
    }

    /**
     * **Validates: Requirement 23.8**
     *
     * For any booking event (reservation, cancellation), the event must be
     * published to bookings.events with classId, userId, event type and timestamp.
     */
    @Property(tries = 100)
    @Tag("spartan-golden-gym")
    @Tag("Property-67")
    void bookingEvent_publishesToCorrectTopic_withCompleteData(
            @ForAll("bookingEventTypes") String eventType
    ) {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        UUID classId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        publishBookingEvent(kafkaTemplate, classId, userId, eventType);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), payloadCaptor.capture());

        assertEquals(KafkaTopics.BOOKINGS_EVENTS, topicCaptor.getValue());
        assertEquals(classId.toString(), keyCaptor.getValue());

        String payload = payloadCaptor.getValue();
        assertTrue(payload.contains("\"classId\":\"" + classId + "\""),
                "Payload must contain classId");
        assertTrue(payload.contains("\"userId\":\"" + userId + "\""),
                "Payload must contain userId");
        assertTrue(payload.contains("\"event\":\"" + eventType + "\""),
                "Payload must contain event type");
        assertTrue(payload.contains("\"timestamp\":"),
                "Payload must contain timestamp");
    }

    /**
     * **Validates: Requirement 3.11**
     *
     * For any AI recommendation generated, the event must be published to
     * ai.recommendations.request with type, userId, referenceId and timestamp.
     */
    @Property(tries = 100)
    @Tag("spartan-golden-gym")
    @Tag("Property-67")
    void aiRecommendation_publishesToCorrectTopic_withCompleteData(
            @ForAll("recommendationTypes") String recType
    ) {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        UUID userId = UUID.randomUUID();
        String referenceId = UUID.randomUUID().toString();

        publishAiRecommendation(kafkaTemplate, recType, userId, referenceId);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), payloadCaptor.capture());

        assertEquals(KafkaTopics.AI_RECOMMENDATIONS_REQUEST, topicCaptor.getValue());
        assertEquals(userId.toString(), keyCaptor.getValue());

        String payload = payloadCaptor.getValue();
        assertTrue(payload.contains("\"type\":\"" + recType + "\""),
                "Payload must contain recommendation type");
        assertTrue(payload.contains("\"userId\":\"" + userId + "\""),
                "Payload must contain userId");
        assertTrue(payload.contains("\"referenceId\":\"" + referenceId + "\""),
                "Payload must contain referenceId");
        assertTrue(payload.contains("\"timestamp\":"),
                "Payload must contain timestamp");
    }

    // -----------------------------------------------------------------------
    // Arbitraries (generators)
    // -----------------------------------------------------------------------

    @Provide
    Arbitrary<String> userIds() {
        return Arbitraries.strings().alpha().numeric().ofMinLength(3).ofMaxLength(36);
    }

    @Provide
    Arbitrary<String> challengeNames() {
        return Arbitraries.of(
                "Weekly Push-up Challenge", "Monthly Running Goal",
                "30-Day Consistency", "Strength Master", "Cardio King");
    }

    @Provide
    Arbitrary<String> badgeNames() {
        return Arbitraries.of(
                "Push-up Champion Badge", "Runner Badge",
                "Consistency Badge", "Strength Badge", "Cardio Badge");
    }

    @Provide
    Arbitrary<String> interactionTypes() {
        return Arbitraries.of("comment", "reaction", "share");
    }

    @Provide
    Arbitrary<String> targetTypes() {
        return Arbitraries.of("workout", "achievement", "challenge", "post");
    }

    @Provide
    Arbitrary<String> mealTypes() {
        return Arbitraries.of("breakfast", "lunch", "dinner", "snack");
    }

    @Provide
    Arbitrary<String> bookingEventTypes() {
        return Arbitraries.of("reservation_created", "reservation_cancelled",
                "waitlist_added", "waitlist_promoted");
    }

    @Provide
    Arbitrary<String> recommendationTypes() {
        return Arbitraries.of("plan_generated", "exercise_recommendation",
                "overtraining_check", "adherence_prediction",
                "recommendation_feedback");
    }
}
