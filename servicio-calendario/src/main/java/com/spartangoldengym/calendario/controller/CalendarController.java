package com.spartangoldengym.calendario.controller;

import com.spartangoldengym.calendario.dto.*;
import com.spartangoldengym.calendario.service.CalendarService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    // --- Events CRUD ---

    @GetMapping("/events")
    public ResponseEntity<List<EventResponse>> getEvents(
            @RequestParam UUID userId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to) {
        List<EventResponse> events = calendarService.getEvents(userId, from, to);
        return ResponseEntity.ok(events);
    }

    @PostMapping("/events")
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody CreateEventRequest request) {
        EventResponse response = calendarService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/events/{id}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEventRequest request) {
        EventResponse response = calendarService.updateEvent(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id) {
        calendarService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    // --- Sync ---

    @PostMapping("/sync")
    public ResponseEntity<SyncResponse> syncCalendar(@Valid @RequestBody SyncRequest request) {
        SyncResponse response = calendarService.syncExternalCalendar(request);
        return ResponseEntity.ok(response);
    }

    // --- Conflicts ---

    @GetMapping("/conflicts")
    public ResponseEntity<ConflictResponse> detectConflicts(@RequestParam UUID userId) {
        ConflictResponse response = calendarService.detectConflicts(userId);
        return ResponseEntity.ok(response);
    }
}
