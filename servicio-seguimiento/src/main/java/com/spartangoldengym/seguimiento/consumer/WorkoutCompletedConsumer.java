package com.spartangoldengym.seguimiento.consumer;

import com.spartangoldengym.common.config.KafkaTopics;
import com.spartangoldengym.seguimiento.service.TrainerLookupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Kafka consumer that listens to workout.completed events.
 * When a client with an assigned trainer completes a workout,
 * sends a notification to the trainer with the session summary.
 *
 * Validates: Requirement 10.3
 */
@Component
public class WorkoutCompletedConsumer {

    private static final Logger log = LoggerFactory.getLogger(WorkoutCompletedConsumer.class);

    private final TrainerLookupService trainerLookupService;

    public WorkoutCompletedConsumer(TrainerLookupService trainerLookupService) {
        this.trainerLookupService = trainerLookupService;
    }

    @KafkaListener(topics = KafkaTopics.WORKOUT_COMPLETED, groupId = "seguimiento-trainer-notify")
    public void onWorkoutCompleted(String message) {
        processWorkoutCompleted(message);
    }

    /**
     * Processes a workout.completed event. Extracted for testability.
     * Returns true if a trainer notification was sent, false otherwise.
     */
    public boolean processWorkoutCompleted(String message) {
        log.info("Received workout.completed event: {}", message);

        String userId = extractField(message, "userId");
        if (userId == null) {
            log.warn("workout.completed event missing userId, skipping: {}", message);
            return false;
        }

        Optional<String> trainerId = trainerLookupService.findTrainerForUser(userId);
        if (!trainerId.isPresent()) {
            log.debug("User {} has no assigned trainer, skipping notification", userId);
            return false;
        }

        String sessionId = extractField(message, "sessionId");
        String durationSeconds = extractField(message, "durationSeconds");
        String exercises = extractField(message, "exercises");
        String sets = extractField(message, "sets");
        String caloriesBurned = extractField(message, "caloriesBurned");

        String summary = String.format(
                "Client %s completed workout (session=%s): duration=%ss, exercises=%s, sets=%s, calories=%s",
                userId, sessionId, durationSeconds, exercises, sets, caloriesBurned);

        notifyTrainer(trainerId.get(), userId, summary);
        return true;
    }

    /**
     * Sends a notification to the trainer. Stub implementation logs the notification.
     * In production, this would call Servicio_Notificaciones.
     */
    void notifyTrainer(String trainerId, String clientId, String summary) {
        log.info("Notifying trainer {} about client {} workout completion: {}",
                trainerId, clientId, summary);
        // Stub: in production, POST to /api/notifications or publish to a Kafka topic
    }

    /**
     * Simple JSON field extractor for flat JSON strings.
     * Handles both string and numeric values.
     */
    static String extractField(String json, String field) {
        if (json == null || field == null) {
            return null;
        }
        String key = "\"" + field + "\":";
        int idx = json.indexOf(key);
        if (idx < 0) {
            return null;
        }
        int start = idx + key.length();
        // Skip whitespace
        while (start < json.length() && json.charAt(start) == ' ') {
            start++;
        }
        if (start >= json.length()) {
            return null;
        }
        if (json.charAt(start) == '"') {
            // String value
            int end = json.indexOf('"', start + 1);
            return end > start ? json.substring(start + 1, end) : null;
        } else {
            // Numeric value
            int end = start;
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') {
                end++;
            }
            return json.substring(start, end).trim();
        }
    }
}
