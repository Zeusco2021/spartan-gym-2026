package com.spartangoldengym.reservas.service;

import com.spartangoldengym.common.config.KafkaTopics;
import com.spartangoldengym.common.exception.ConflictException;
import com.spartangoldengym.common.exception.ResourceNotFoundException;
import com.spartangoldengym.reservas.dto.*;
import com.spartangoldengym.reservas.entity.ClassReservation;
import com.spartangoldengym.reservas.entity.GroupClass;
import com.spartangoldengym.reservas.entity.Waitlist;
import com.spartangoldengym.reservas.repository.ClassReservationRepository;
import com.spartangoldengym.reservas.repository.GroupClassRepository;
import com.spartangoldengym.reservas.repository.WaitlistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);
    private static final long CANCELLATION_GRACE_HOURS = 2;

    private final GroupClassRepository classRepository;
    private final ClassReservationRepository reservationRepository;
    private final WaitlistRepository waitlistRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public BookingService(GroupClassRepository classRepository,
                          ClassReservationRepository reservationRepository,
                          WaitlistRepository waitlistRepository,
                          KafkaTemplate<String, String> kafkaTemplate) {
        this.classRepository = classRepository;
        this.reservationRepository = reservationRepository;
        this.waitlistRepository = waitlistRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public ClassResponse createClass(CreateClassRequest request) {
        GroupClass gc = new GroupClass();
        gc.setGymId(request.getGymId());
        gc.setInstructorId(request.getInstructorId());
        gc.setName(request.getName());
        gc.setRoom(request.getRoom());
        gc.setMaxCapacity(request.getMaxCapacity());
        gc.setCurrentCapacity(0);
        gc.setDifficultyLevel(request.getDifficultyLevel());
        gc.setScheduledAt(request.getScheduledAt());
        gc.setDurationMinutes(request.getDurationMinutes());
        gc = classRepository.save(gc);

        publishEvent(gc.getId(), null, "class_created");
        log.info("Group class created: id={} name={}", gc.getId(), gc.getName());
        return toClassResponse(gc);
    }

    @Transactional(readOnly = true)
    public List<ClassResponse> listClasses(String name, UUID instructorId, Instant from, Instant to,
                                           String difficultyLevel, UUID gymId) {
        Specification<GroupClass> spec = Specification.where(null);

        if (name != null && !name.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%"));
        }
        if (instructorId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("instructorId"), instructorId));
        }
        if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("scheduledAt"), from));
        }
        if (to != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("scheduledAt"), to));
        }
        if (difficultyLevel != null && !difficultyLevel.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("difficultyLevel"), difficultyLevel));
        }
        if (gymId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("gymId"), gymId));
        }

        return classRepository.findAll(spec).stream()
                .map(this::toClassResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReservationResponse reserveClass(UUID classId, ReserveRequest request) {
        GroupClass gc = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("GroupClass", classId.toString()));

        // Check for duplicate reservation
        reservationRepository.findByClassIdAndUserIdAndStatus(classId, request.getUserId(), "confirmed")
                .ifPresent(existing -> {
                    throw new ConflictException("User already has a confirmed reservation for this class",
                            "DUPLICATE_RESERVATION");
                });

        if (gc.getCurrentCapacity() < gc.getMaxCapacity()) {
            // Spot available — confirm reservation
            gc.setCurrentCapacity(gc.getCurrentCapacity() + 1);
            classRepository.save(gc);

            ClassReservation reservation = new ClassReservation();
            reservation.setClassId(classId);
            reservation.setUserId(request.getUserId());
            reservation.setStatus("confirmed");
            reservation.setReservedAt(Instant.now());
            reservation = reservationRepository.save(reservation);

            publishEvent(classId, request.getUserId(), "reservation_confirmed");
            log.info("Reservation confirmed: classId={} userId={}", classId, request.getUserId());
            return toReservationResponse(reservation, null);
        } else {
            // Class full — add to waitlist
            int nextPosition = waitlistRepository.countByClassId(classId) + 1;
            Waitlist wl = new Waitlist();
            wl.setClassId(classId);
            wl.setUserId(request.getUserId());
            wl.setPosition(nextPosition);
            wl.setAddedAt(Instant.now());
            waitlistRepository.save(wl);

            publishEvent(classId, request.getUserId(), "added_to_waitlist");
            log.info("User added to waitlist: classId={} userId={} position={}", classId, request.getUserId(), nextPosition);

            ReservationResponse response = new ReservationResponse();
            response.setClassId(classId);
            response.setUserId(request.getUserId());
            response.setStatus("waitlisted");
            response.setWaitlistPosition(nextPosition);
            return response;
        }
    }

    @Transactional
    public ReservationResponse cancelReservation(UUID classId, CancelReservationRequest request) {
        GroupClass gc = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("GroupClass", classId.toString()));

        ClassReservation reservation = reservationRepository
                .findByClassIdAndUserIdAndStatus(classId, request.getUserId(), "confirmed")
                .orElseThrow(() -> new ResourceNotFoundException("ClassReservation",
                        "classId=" + classId + ",userId=" + request.getUserId()));

        Instant now = Instant.now();
        boolean lateCancellation = gc.getScheduledAt().minus(CANCELLATION_GRACE_HOURS, ChronoUnit.HOURS)
                .isBefore(now);

        reservation.setStatus("cancelled");
        reservation.setCancelledAt(now);

        if (lateCancellation) {
            reservation.setPenaltyCount(reservation.getPenaltyCount() + 1);
            log.info("Late cancellation penalty: classId={} userId={}", classId, request.getUserId());
        }

        reservationRepository.save(reservation);

        // Decrement capacity and promote waitlist user
        gc.setCurrentCapacity(Math.max(0, gc.getCurrentCapacity() - 1));
        classRepository.save(gc);

        promoteFromWaitlist(classId, gc);

        String eventType = lateCancellation ? "reservation_cancelled_late" : "reservation_cancelled";
        publishEvent(classId, request.getUserId(), eventType);
        log.info("Reservation cancelled: classId={} userId={} late={}", classId, request.getUserId(), lateCancellation);
        return toReservationResponse(reservation, null);
    }

    private void promoteFromWaitlist(UUID classId, GroupClass gc) {
        waitlistRepository.findFirstByClassIdOrderByPositionAsc(classId).ifPresent(first -> {
            // Create confirmed reservation for waitlisted user
            ClassReservation promoted = new ClassReservation();
            promoted.setClassId(classId);
            promoted.setUserId(first.getUserId());
            promoted.setStatus("confirmed");
            promoted.setReservedAt(Instant.now());
            reservationRepository.save(promoted);

            gc.setCurrentCapacity(gc.getCurrentCapacity() + 1);
            classRepository.save(gc);

            waitlistRepository.delete(first);

            publishEvent(classId, first.getUserId(), "promoted_from_waitlist");
            log.info("User promoted from waitlist: classId={} userId={}", classId, first.getUserId());
        });
    }

    @Transactional(readOnly = true)
    public WaitlistResponse getWaitlist(UUID classId) {
        classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("GroupClass", classId.toString()));

        List<Waitlist> entries = waitlistRepository.findByClassIdOrderByPositionAsc(classId);

        WaitlistResponse response = new WaitlistResponse();
        response.setClassId(classId);
        response.setTotalWaiting(entries.size());
        response.setEntries(entries.stream().map(w -> {
            WaitlistResponse.WaitlistEntry entry = new WaitlistResponse.WaitlistEntry();
            entry.setUserId(w.getUserId());
            entry.setPosition(w.getPosition());
            entry.setAddedAt(w.getAddedAt());
            return entry;
        }).collect(Collectors.toList()));
        return response;
    }

    private void publishEvent(UUID classId, UUID userId, String eventType) {
        String userField = userId != null ? ",\"userId\":\"" + userId + "\"" : "";
        String event = "{\"classId\":\"" + classId + "\"" + userField
                + ",\"event\":\"" + eventType
                + "\",\"timestamp\":\"" + Instant.now() + "\"}";
        kafkaTemplate.send(KafkaTopics.BOOKINGS_EVENTS, classId.toString(), event);
    }

    private ClassResponse toClassResponse(GroupClass gc) {
        ClassResponse r = new ClassResponse();
        r.setId(gc.getId());
        r.setGymId(gc.getGymId());
        r.setInstructorId(gc.getInstructorId());
        r.setName(gc.getName());
        r.setRoom(gc.getRoom());
        r.setMaxCapacity(gc.getMaxCapacity());
        r.setCurrentCapacity(gc.getCurrentCapacity());
        r.setDifficultyLevel(gc.getDifficultyLevel());
        r.setScheduledAt(gc.getScheduledAt());
        r.setDurationMinutes(gc.getDurationMinutes());
        return r;
    }

    private ReservationResponse toReservationResponse(ClassReservation res, Integer waitlistPosition) {
        ReservationResponse r = new ReservationResponse();
        r.setId(res.getId());
        r.setClassId(res.getClassId());
        r.setUserId(res.getUserId());
        r.setStatus(res.getStatus());
        r.setPenaltyCount(res.getPenaltyCount());
        r.setReservedAt(res.getReservedAt());
        r.setCancelledAt(res.getCancelledAt());
        r.setWaitlistPosition(waitlistPosition);
        return r;
    }
}
