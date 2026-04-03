package com.spartangoldengym.reservas.controller;

import com.spartangoldengym.reservas.dto.*;
import com.spartangoldengym.reservas.service.BookingService;
import com.spartangoldengym.reservas.service.TrainerAvailabilityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final TrainerAvailabilityService trainerAvailabilityService;

    public BookingController(BookingService bookingService,
                             TrainerAvailabilityService trainerAvailabilityService) {
        this.bookingService = bookingService;
        this.trainerAvailabilityService = trainerAvailabilityService;
    }

    // --- Group Classes ---

    @PostMapping("/classes")
    public ResponseEntity<ClassResponse> createClass(@Valid @RequestBody CreateClassRequest request) {
        ClassResponse response = bookingService.createClass(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/classes")
    public ResponseEntity<List<ClassResponse>> listClasses(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) UUID instructorId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) String difficultyLevel,
            @RequestParam(required = false) UUID gymId) {
        List<ClassResponse> classes = bookingService.listClasses(name, instructorId, from, to,
                difficultyLevel, gymId);
        return ResponseEntity.ok(classes);
    }

    // --- Reservations ---

    @PostMapping("/classes/{id}/reserve")
    public ResponseEntity<ReservationResponse> reserveClass(
            @PathVariable UUID id,
            @Valid @RequestBody ReserveRequest request) {
        ReservationResponse response = bookingService.reserveClass(id, request);
        HttpStatus status = "waitlisted".equals(response.getStatus())
                ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/classes/{id}/cancel")
    public ResponseEntity<ReservationResponse> cancelReservation(
            @PathVariable UUID id,
            @Valid @RequestBody CancelReservationRequest request) {
        ReservationResponse response = bookingService.cancelReservation(id, request);
        return ResponseEntity.ok(response);
    }

    // --- Waitlist ---

    @GetMapping("/waitlist/{classId}")
    public ResponseEntity<WaitlistResponse> getWaitlist(@PathVariable UUID classId) {
        WaitlistResponse response = bookingService.getWaitlist(classId);
        return ResponseEntity.ok(response);
    }

    // --- Trainer Availability ---

    @GetMapping("/trainers/{id}/availability")
    public ResponseEntity<TrainerAvailabilityResponse> getTrainerAvailability(@PathVariable UUID id) {
        TrainerAvailabilityResponse response = trainerAvailabilityService.getAvailability(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/trainers/{id}/availability")
    public ResponseEntity<TrainerAvailabilityResponse> updateTrainerAvailability(
            @PathVariable UUID id,
            @Valid @RequestBody TrainerAvailabilityRequest request) {
        TrainerAvailabilityResponse response = trainerAvailabilityService.updateAvailability(id, request);
        return ResponseEntity.ok(response);
    }
}
