package com.spartangoldengym.calendario.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartangoldengym.calendario.service.CalendarService;
import com.spartangoldengym.common.config.KafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Consumes booking events from Kafka to auto-create calendar events
 * when a user confirms a class reservation.
 */
@Component
public class BookingEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(BookingEventConsumer.class);

    private final CalendarService calendarService;
    private final ObjectMapper objectMapper;

    public BookingEventConsumer(CalendarService calendarService) {
        this.calendarService = calendarService;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = KafkaTopics.BOOKINGS_EVENTS, groupId = "servicio-calendario")
    public void onBookingEvent(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String eventType = node.path("event").asText();

            if ("reservation_confirmed".equals(eventType) || "promoted_from_waitlist".equals(eventType)) {
                UUID userId = UUID.fromString(node.path("userId").asText());
                UUID classId = UUID.fromString(node.path("classId").asText());
                String className = node.has("className") ? node.path("className").asText() : "Group Class";
                Instant scheduledAt = node.has("scheduledAt")
                        ? Instant.parse(node.path("scheduledAt").asText())
                        : Instant.now();
                int duration = node.has("durationMinutes") ? node.path("durationMinutes").asInt() : 60;

                calendarService.createEventFromBooking(userId, classId, className, scheduledAt, duration);
                log.info("Calendar event created from booking event: classId={} userId={}", classId, userId);
            }
        } catch (Exception e) {
            log.error("Failed to process booking event: {}", message, e);
        }
    }
}
