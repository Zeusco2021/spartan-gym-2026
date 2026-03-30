package com.spartangoldengym.notificaciones.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartangoldengym.common.config.KafkaTopics;
import com.spartangoldengym.notificaciones.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer that listens to multiple topics and triggers notifications.
 * Applies user preference rules before sending.
 *
 * Validates: Requirements 22.2, 22.5
 */
@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public NotificationEventConsumer(NotificationService notificationService,
                                     ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = KafkaTopics.WORKOUT_COMPLETED, groupId = "notificaciones-group")
    public void onWorkoutCompleted(String message) {
        processEvent(message, "entrenamientos", "Entrenamiento completado",
                "Has completado tu sesión de entrenamiento", false);
    }

    @KafkaListener(topics = KafkaTopics.USER_ACHIEVEMENTS, groupId = "notificaciones-group")
    public void onUserAchievement(String message) {
        processEvent(message, "social", "Nuevo logro desbloqueado",
                "Has desbloqueado un nuevo logro", false);
    }

    @KafkaListener(topics = KafkaTopics.BOOKINGS_EVENTS, groupId = "notificaciones-group")
    public void onBookingEvent(String message) {
        processEvent(message, "entrenamientos", "Actualización de reserva",
                "Tu reserva ha sido actualizada", false);
    }

    @KafkaListener(topics = KafkaTopics.NUTRITION_LOGS, groupId = "notificaciones-group")
    public void onNutritionLog(String message) {
        processEvent(message, "nutricion", "Registro nutricional",
                "Se ha registrado tu comida", false);
    }

    @KafkaListener(topics = KafkaTopics.SOCIAL_INTERACTIONS, groupId = "notificaciones-group")
    public void onSocialInteraction(String message) {
        processEvent(message, "social", "Nueva interacción social",
                "Tienes una nueva interacción en la comunidad", false);
    }

    private void processEvent(String message, String category, String defaultTitle,
                              String defaultContent, boolean urgent) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String userId = node.has("userId") ? node.get("userId").asText() : null;
            String title = node.has("title") ? node.get("title").asText() : defaultTitle;
            String content = node.has("content") ? node.get("content").asText() : defaultContent;

            if (userId == null) {
                log.warn("Received event without userId, skipping: {}", message);
                return;
            }

            notificationService.sendNotification(userId, title, content, category, urgent);
        } catch (Exception e) {
            log.error("Error processing notification event: {}", e.getMessage(), e);
        }
    }
}
