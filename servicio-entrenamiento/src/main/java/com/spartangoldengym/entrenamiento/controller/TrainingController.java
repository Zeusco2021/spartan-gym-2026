package com.spartangoldengym.entrenamiento.controller;

import com.spartangoldengym.entrenamiento.dto.*;
import com.spartangoldengym.entrenamiento.service.ExerciseService;
import com.spartangoldengym.entrenamiento.service.RoutineService;
import com.spartangoldengym.entrenamiento.service.TrainingPlanService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/training")
public class TrainingController {

    private final TrainingPlanService planService;
    private final ExerciseService exerciseService;
    private final RoutineService routineService;

    public TrainingController(TrainingPlanService planService,
                              ExerciseService exerciseService,
                              RoutineService routineService) {
        this.planService = planService;
        this.exerciseService = exerciseService;
        this.routineService = routineService;
    }

    // --- Training Plans ---

    @PostMapping("/plans")
    public ResponseEntity<TrainingPlanResponse> createPlan(
            @Valid @RequestBody CreateTrainingPlanRequest request) {
        TrainingPlanResponse response = planService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/plans")
    public ResponseEntity<List<TrainingPlanResponse>> listPlans(
            @RequestParam(required = false) UUID userId) {
        List<TrainingPlanResponse> plans = planService.listPlans(userId);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/plans/{id}")
    public ResponseEntity<TrainingPlanResponse> getPlan(@PathVariable UUID id) {
        TrainingPlanResponse response = planService.getPlan(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/plans/{id}")
    public ResponseEntity<TrainingPlanResponse> updatePlan(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTrainingPlanRequest request) {
        TrainingPlanResponse response = planService.updatePlan(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/plans/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable UUID id) {
        planService.deletePlan(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/plans/{id}/assign")
    public ResponseEntity<TrainingPlanResponse> assignPlan(
            @PathVariable UUID id,
            @Valid @RequestBody AssignPlanRequest request) {
        TrainingPlanResponse response = planService.assignPlan(id, request);
        return ResponseEntity.ok(response);
    }

    // --- Exercises ---

    @GetMapping("/exercises")
    public ResponseEntity<Page<ExerciseResponse>> listExercises(Pageable pageable) {
        Page<ExerciseResponse> exercises = exerciseService.listExercises(pageable);
        return ResponseEntity.ok(exercises);
    }

    // --- Routines ---

    @PostMapping("/routines")
    public ResponseEntity<RoutineResponse> createRoutine(
            @Valid @RequestBody CreateRoutineRequest request) {
        RoutineResponse response = routineService.createRoutine(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/routines")
    public ResponseEntity<List<RoutineResponse>> listRoutines(
            @RequestParam(required = false) UUID planId) {
        List<RoutineResponse> routines = routineService.listRoutines(planId);
        return ResponseEntity.ok(routines);
    }
}
