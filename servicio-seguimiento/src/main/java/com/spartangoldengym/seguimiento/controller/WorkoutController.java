package com.spartangoldengym.seguimiento.controller;

import com.spartangoldengym.seguimiento.dto.*;
import com.spartangoldengym.seguimiento.service.WorkoutService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for workout session management.
 *
 * Validates: Requirements 4.1, 4.2, 4.3, 4.5, 4.6, 4.9, 8.4
 */
@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @PostMapping("/start")
    public ResponseEntity<WorkoutSessionResponse> startWorkout(@RequestBody StartWorkoutRequest request) {
        WorkoutSessionResponse response = workoutService.startWorkout(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/sets")
    public ResponseEntity<WorkoutSetResponse> recordSet(
            @PathVariable("id") String sessionId,
            @RequestBody RecordSetRequest request) {
        WorkoutSetResponse response = workoutService.recordSet(sessionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/heartrate")
    public ResponseEntity<Void> recordHeartRate(
            @PathVariable("id") String sessionId,
            @RequestBody RecordHeartRateRequest request) {
        workoutService.recordHeartRate(sessionId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<WorkoutSessionResponse> completeWorkout(@PathVariable("id") String sessionId) {
        WorkoutSessionResponse response = workoutService.completeWorkout(sessionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<WorkoutSessionResponse>> getHistory(@RequestParam("userId") String userId) {
        List<WorkoutSessionResponse> history = workoutService.getWorkoutHistory(userId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/progress")
    public ResponseEntity<WorkoutProgressResponse> getProgress(@RequestParam("userId") String userId) {
        WorkoutProgressResponse progress = workoutService.getProgress(userId);
        return ResponseEntity.ok(progress);
    }
}
