package com.spartangoldengym.calendario.service;

import com.spartangoldengym.calendario.dto.*;
import com.spartangoldengym.calendario.entity.CalendarEvent;
import com.spartangoldengym.calendario.repository.CalendarEventRepository;
import com.spartangoldengym.calendario.sync.ExternalCalendarProvider;
import com.spartangoldengym.common.exception.ConflictException;
import com.spartangoldengym.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CalendarServiceTest {

    private CalendarEventRepository eventRepository;
    private KafkaTemplate<String, String> kafkaTemplate;
    private CalendarService calendarService;

    private final UUID userId = UUID.randomUUID();
    private final Instant now = Instant.now();

    @BeforeEach
    void setUp() {
        eventRepository = mock(CalendarEventRepository.class);
        kafkaTemplate = mock(KafkaTemplate.class);

        ExternalCalendarProvider googleProvider = mock(ExternalCalendarProvider.class);
        when(googleProvider.getProviderName()).thenReturn("google");
        SyncResponse syncResp = new SyncResponse();
        syncResp.setProvider("google");
        syncResp.setStatus("synced");
        when(googleProvider.sync(any(), any())).thenReturn(syncResp);

        calendarService = new CalendarService(
                eventRepository,
                Collections.singletonList(googleProvider),
                kafkaTemplate);
    }

    @Test
    void createEvent_noConflict_savesAndReturnsEvent() {
        CreateEventRequest request = new CreateEventRequest();
        request.setUserId(userId);
        request.setEventType("workout");
        request.setTitle("Morning Workout");
        request.setStartsAt(now.plus(1, ChronoUnit.HOURS));
        request.setEndsAt(now.plus(2, ChronoUnit.HOURS));
        request.setReminderMinutes(30);

        when(eventRepository.findOverlapping(eq(userId), any(), any()))
                .thenReturn(Collections.emptyList());

        CalendarEvent saved = buildEvent(userId, "workout", "Morning Workout",
                request.getStartsAt(), request.getEndsAt(), 30);
        when(eventRepository.save(any(CalendarEvent.class))).thenReturn(saved);

        EventResponse response = calendarService.createEvent(request);

        assertNotNull(response);
        assertEquals("Morning Workout", response.getTitle());
        assertEquals("workout", response.getEventType());
        verify(eventRepository).save(any(CalendarEvent.class));
        verify(kafkaTemplate).send(eq("notifications.schedule"), anyString(), anyString());
    }

    @Test
    void createEvent_withConflict_throwsConflictException() {
        CreateEventRequest request = new CreateEventRequest();
        request.setUserId(userId);
        request.setEventType("class");
        request.setTitle("Yoga Class");
        request.setStartsAt(now.plus(1, ChronoUnit.HOURS));
        request.setEndsAt(now.plus(2, ChronoUnit.HOURS));

        CalendarEvent existing = buildEvent(userId, "workout", "Existing",
                now.plus(90, ChronoUnit.MINUTES), now.plus(150, ChronoUnit.MINUTES), 30);
        when(eventRepository.findOverlapping(eq(userId), any(), any()))
                .thenReturn(Collections.singletonList(existing));

        assertThrows(ConflictException.class, () -> calendarService.createEvent(request));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateEvent_notFound_throwsResourceNotFoundException() {
        UUID eventId = UUID.randomUUID();
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        UpdateEventRequest request = new UpdateEventRequest();
        request.setTitle("Updated");

        assertThrows(ResourceNotFoundException.class, () -> calendarService.updateEvent(eventId, request));
    }

    @Test
    void updateEvent_updatesFieldsSuccessfully() {
        UUID eventId = UUID.randomUUID();
        CalendarEvent existing = buildEvent(userId, "workout", "Old Title",
                now.plus(1, ChronoUnit.HOURS), now.plus(2, ChronoUnit.HOURS), 30);
        existing.setId(eventId);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existing));
        when(eventRepository.save(any(CalendarEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateEventRequest request = new UpdateEventRequest();
        request.setTitle("New Title");
        request.setReminderMinutes(15);

        EventResponse response = calendarService.updateEvent(eventId, request);

        assertEquals("New Title", response.getTitle());
        assertEquals(15, response.getReminderMinutes());
    }

    @Test
    void deleteEvent_notFound_throwsResourceNotFoundException() {
        UUID eventId = UUID.randomUUID();
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> calendarService.deleteEvent(eventId));
    }

    @Test
    void deleteEvent_deletesSuccessfully() {
        UUID eventId = UUID.randomUUID();
        CalendarEvent existing = buildEvent(userId, "class", "Yoga", now, now.plus(1, ChronoUnit.HOURS), 30);
        existing.setId(eventId);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existing));

        calendarService.deleteEvent(eventId);

        verify(eventRepository).delete(existing);
    }

    @Test
    void detectConflicts_noOverlaps_returnsEmpty() {
        CalendarEvent e1 = buildEvent(userId, "workout", "A",
                now, now.plus(1, ChronoUnit.HOURS), 30);
        CalendarEvent e2 = buildEvent(userId, "class", "B",
                now.plus(2, ChronoUnit.HOURS), now.plus(3, ChronoUnit.HOURS), 30);
        when(eventRepository.findByUserIdOrderByStartsAtAsc(userId))
                .thenReturn(Arrays.asList(e1, e2));

        ConflictResponse response = calendarService.detectConflicts(userId);

        assertEquals(0, response.getTotalConflicts());
        assertTrue(response.getConflicts().isEmpty());
    }

    @Test
    void detectConflicts_withOverlap_returnsConflictPair() {
        CalendarEvent e1 = buildEvent(userId, "workout", "A",
                now, now.plus(2, ChronoUnit.HOURS), 30);
        CalendarEvent e2 = buildEvent(userId, "class", "B",
                now.plus(1, ChronoUnit.HOURS), now.plus(3, ChronoUnit.HOURS), 30);
        when(eventRepository.findByUserIdOrderByStartsAtAsc(userId))
                .thenReturn(Arrays.asList(e1, e2));

        ConflictResponse response = calendarService.detectConflicts(userId);

        assertEquals(1, response.getTotalConflicts());
        assertEquals("A", response.getConflicts().get(0).getEventA().getTitle());
        assertEquals("B", response.getConflicts().get(0).getEventB().getTitle());
    }

    @Test
    void syncExternalCalendar_validProvider_returnsSyncResponse() {
        SyncRequest request = new SyncRequest();
        request.setUserId(userId);
        request.setProvider("google");
        request.setAccessToken("token123");

        SyncResponse response = calendarService.syncExternalCalendar(request);

        assertEquals("google", response.getProvider());
        assertEquals("synced", response.getStatus());
    }

    @Test
    void syncExternalCalendar_unsupportedProvider_throwsException() {
        SyncRequest request = new SyncRequest();
        request.setUserId(userId);
        request.setProvider("unknown");
        request.setAccessToken("token");

        assertThrows(IllegalArgumentException.class, () -> calendarService.syncExternalCalendar(request));
    }

    @Test
    void createEvent_invalidReminderMinutes_defaultsTo30() {
        CreateEventRequest request = new CreateEventRequest();
        request.setUserId(userId);
        request.setEventType("workout");
        request.setTitle("Test");
        request.setStartsAt(now.plus(5, ChronoUnit.HOURS));
        request.setEndsAt(now.plus(6, ChronoUnit.HOURS));
        request.setReminderMinutes(45); // invalid

        when(eventRepository.findOverlapping(eq(userId), any(), any()))
                .thenReturn(Collections.emptyList());

        CalendarEvent saved = buildEvent(userId, "workout", "Test",
                request.getStartsAt(), request.getEndsAt(), 30);
        when(eventRepository.save(any(CalendarEvent.class))).thenReturn(saved);

        EventResponse response = calendarService.createEvent(request);
        assertEquals(30, response.getReminderMinutes());
    }

    @Test
    void getEvents_withDateRange_delegatesToRepository() {
        Instant from = now;
        Instant to = now.plus(7, ChronoUnit.DAYS);
        when(eventRepository.findByUserIdAndStartsAtBetweenOrderByStartsAtAsc(userId, from, to))
                .thenReturn(Collections.emptyList());

        List<EventResponse> events = calendarService.getEvents(userId, from, to);

        assertTrue(events.isEmpty());
        verify(eventRepository).findByUserIdAndStartsAtBetweenOrderByStartsAtAsc(userId, from, to);
    }

    @Test
    void getEvents_withoutDateRange_returnsAllEvents() {
        when(eventRepository.findByUserIdOrderByStartsAtAsc(userId))
                .thenReturn(Collections.emptyList());

        List<EventResponse> events = calendarService.getEvents(userId, null, null);

        assertTrue(events.isEmpty());
        verify(eventRepository).findByUserIdOrderByStartsAtAsc(userId);
    }

    @Test
    void createEventFromBooking_createsClassEvent() {
        UUID classId = UUID.randomUUID();
        Instant scheduledAt = now.plus(1, ChronoUnit.DAYS);

        CalendarEvent saved = buildEvent(userId, "class", "Class: Yoga",
                scheduledAt, scheduledAt.plusSeconds(3600), 30);
        when(eventRepository.save(any(CalendarEvent.class))).thenReturn(saved);

        calendarService.createEventFromBooking(userId, classId, "Yoga", scheduledAt, 60);

        verify(eventRepository).save(any(CalendarEvent.class));
        verify(kafkaTemplate).send(eq("notifications.schedule"), anyString(), anyString());
    }

    private CalendarEvent buildEvent(UUID userId, String type, String title,
                                     Instant startsAt, Instant endsAt, int reminder) {
        CalendarEvent event = new CalendarEvent();
        event.setId(UUID.randomUUID());
        event.setUserId(userId);
        event.setEventType(type);
        event.setTitle(title);
        event.setStartsAt(startsAt);
        event.setEndsAt(endsAt);
        event.setReminderMinutes(reminder);
        event.setCreatedAt(Instant.now());
        return event;
    }
}
