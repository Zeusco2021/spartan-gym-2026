package com.spartangoldengym.calendario.service;

import com.spartangoldengym.calendario.dto.*;
import com.spartangoldengym.calendario.entity.CalendarEvent;
import com.spartangoldengym.calendario.repository.CalendarEventRepository;
import com.spartangoldengym.calendario.sync.ExternalCalendarProvider;
import com.spartangoldengym.common.exception.ConflictException;
import com.spartangoldengym.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core calendar service handling CRUD, conflict detection, external sync,
 * and reminder generation.
 *
 * Validates: Requirements 26.1, 26.2, 26.4, 26.5, 26.6
 */
@Service
public class CalendarService {

    private static final Logger log = LoggerFactory.getLogger(CalendarService.class);
    private static final Set<Integer> VALID_REMINDER_MINUTES = new HashSet<>(Arrays.asList(15, 30, 60));

    private final CalendarEventRepository eventRepository;
    private final Map<String, ExternalCalendarProvider> providerMap;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public CalendarService(CalendarEventRepository eventRepository,
                           List<ExternalCalendarProvider> providers,
                           KafkaTemplate<String, String> kafkaTemplate) {
        this.eventRepository = eventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.providerMap = new HashMap<>();
        for (ExternalCalendarProvider provider : providers) {
            providerMap.put(provider.getProviderName(), provider);
        }
    }

    // --- CRUD ---

    @Transactional(readOnly = true)
    public List<EventResponse> getEvents(UUID userId, Instant from, Instant to) {
        List<CalendarEvent> events;
        if (from != null && to != null) {
            events = eventRepository.findByUserIdAndStartsAtBetweenOrderByStartsAtAsc(userId, from, to);
        } else {
            events = eventRepository.findByUserIdOrderByStartsAtAsc(userId);
        }
        return events.stream().map(this::toEventResponse).collect(Collectors.toList());
    }

    @Transactional
    public EventResponse createEvent(CreateEventRequest request) {
        // Validate reminder minutes
        int reminder = request.getReminderMinutes() != null ? request.getReminderMinutes() : 30;
        if (!VALID_REMINDER_MINUTES.contains(reminder)) {
            reminder = 30;
        }

        // Check for conflicts (Req 26.2)
        List<CalendarEvent> overlapping = eventRepository.findOverlapping(
                request.getUserId(), request.getStartsAt(), request.getEndsAt());
        if (!overlapping.isEmpty()) {
            throw new ConflictException(
                    "Event overlaps with " + overlapping.size() + " existing event(s)",
                    "SCHEDULE_CONFLICT");
        }

        CalendarEvent event = new CalendarEvent();
        event.setUserId(request.getUserId());
        event.setEventType(request.getEventType());
        event.setReferenceId(request.getReferenceId());
        event.setTitle(request.getTitle());
        event.setStartsAt(request.getStartsAt());
        event.setEndsAt(request.getEndsAt());
        event.setReminderMinutes(reminder);
        event = eventRepository.save(event);

        // Schedule reminder via Servicio_Notificaciones
        scheduleReminder(event);

        log.info("Calendar event created: id={} userId={} type={}", event.getId(), event.getUserId(), event.getEventType());
        return toEventResponse(event);
    }

    @Transactional
    public EventResponse updateEvent(UUID eventId, UpdateEventRequest request) {
        CalendarEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("CalendarEvent", eventId.toString()));

        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getEventType() != null) {
            event.setEventType(request.getEventType());
        }
        if (request.getStartsAt() != null) {
            event.setStartsAt(request.getStartsAt());
        }
        if (request.getEndsAt() != null) {
            event.setEndsAt(request.getEndsAt());
        }
        if (request.getReminderMinutes() != null) {
            int reminder = VALID_REMINDER_MINUTES.contains(request.getReminderMinutes())
                    ? request.getReminderMinutes() : 30;
            event.setReminderMinutes(reminder);
        }

        // Re-check conflicts if time changed
        if (request.getStartsAt() != null || request.getEndsAt() != null) {
            List<CalendarEvent> overlapping = eventRepository.findOverlappingExcluding(
                    event.getUserId(), event.getId(), event.getStartsAt(), event.getEndsAt());
            if (!overlapping.isEmpty()) {
                throw new ConflictException(
                        "Updated event overlaps with " + overlapping.size() + " existing event(s)",
                        "SCHEDULE_CONFLICT");
            }
        }

        event = eventRepository.save(event);
        log.info("Calendar event updated: id={}", event.getId());
        return toEventResponse(event);
    }

    @Transactional
    public void deleteEvent(UUID eventId) {
        CalendarEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("CalendarEvent", eventId.toString()));
        eventRepository.delete(event);
        log.info("Calendar event deleted: id={} userId={}", eventId, event.getUserId());
    }

    // --- Conflict detection (Req 26.2) ---

    @Transactional(readOnly = true)
    public ConflictResponse detectConflicts(UUID userId) {
        List<CalendarEvent> events = eventRepository.findByUserIdOrderByStartsAtAsc(userId);
        List<ConflictResponse.ConflictPair> pairs = new ArrayList<>();

        for (int i = 0; i < events.size(); i++) {
            for (int j = i + 1; j < events.size(); j++) {
                CalendarEvent a = events.get(i);
                CalendarEvent b = events.get(j);
                // b starts after a ends — no more overlaps possible since sorted
                if (!b.getStartsAt().isBefore(a.getEndsAt())) {
                    break;
                }
                ConflictResponse.ConflictPair pair = new ConflictResponse.ConflictPair();
                pair.setEventA(toEventResponse(a));
                pair.setEventB(toEventResponse(b));
                pairs.add(pair);
            }
        }

        ConflictResponse response = new ConflictResponse();
        response.setTotalConflicts(pairs.size());
        response.setConflicts(pairs);
        return response;
    }

    // --- External calendar sync (Req 26.4) ---

    public SyncResponse syncExternalCalendar(SyncRequest request) {
        ExternalCalendarProvider provider = providerMap.get(request.getProvider().toLowerCase());
        if (provider == null) {
            throw new IllegalArgumentException("Unsupported calendar provider: " + request.getProvider());
        }
        return provider.sync(request.getUserId(), request.getAccessToken());
    }

    // --- Reminder generation (Req 26.6) ---

    void scheduleReminder(CalendarEvent event) {
        int minutes = event.getReminderMinutes() != null ? event.getReminderMinutes() : 30;
        Instant reminderAt = event.getStartsAt().minusSeconds((long) minutes * 60);

        String notification = String.format(
                "{\"userId\":\"%s\",\"title\":\"Reminder: %s\",\"content\":\"Your event '%s' starts in %d minutes\"," +
                "\"category\":\"calendar\",\"urgent\":false,\"scheduledAt\":\"%s\"}",
                event.getUserId(), event.getTitle(), event.getTitle(), minutes, reminderAt);

        kafkaTemplate.send("notifications.schedule", event.getUserId().toString(), notification);
        log.info("Reminder scheduled for event {} at {}", event.getId(), reminderAt);
    }

    // --- Auto-create from booking events ---

    @Transactional
    public void createEventFromBooking(UUID userId, UUID classId, String className, Instant scheduledAt, int durationMinutes) {
        Instant endsAt = scheduledAt.plusSeconds((long) durationMinutes * 60);

        CalendarEvent event = new CalendarEvent();
        event.setUserId(userId);
        event.setEventType("class");
        event.setReferenceId(classId);
        event.setTitle("Class: " + className);
        event.setStartsAt(scheduledAt);
        event.setEndsAt(endsAt);
        event.setReminderMinutes(30);
        event = eventRepository.save(event);

        scheduleReminder(event);
        log.info("Calendar event auto-created from booking: eventId={} classId={} userId={}", event.getId(), classId, userId);
    }

    // --- Mapping ---

    EventResponse toEventResponse(CalendarEvent event) {
        EventResponse r = new EventResponse();
        r.setId(event.getId());
        r.setUserId(event.getUserId());
        r.setEventType(event.getEventType());
        r.setReferenceId(event.getReferenceId());
        r.setTitle(event.getTitle());
        r.setStartsAt(event.getStartsAt());
        r.setEndsAt(event.getEndsAt());
        r.setReminderMinutes(event.getReminderMinutes());
        r.setExternalCalendarId(event.getExternalCalendarId());
        r.setCreatedAt(event.getCreatedAt());
        return r;
    }
}
