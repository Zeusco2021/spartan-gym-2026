package com.spartangoldengym.iacoach.controller;

import com.spartangoldengym.iacoach.dto.*;
import com.spartangoldengym.iacoach.service.AiCoachService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST controller for AI Coach endpoints.
 * Handles plan generation, exercise recommendations, alternatives, warmup,
 * overtraining detection, adherence prediction, and recommendation feedback.
 *
 * Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9, 3.10, 3.11, 18.1, 18.3, 18.6
 */
@RestController
@RequestMapping("/api/ai")
public class AiCoachController {

    private final AiCoachService aiCoachService;

    public AiCoachController(AiCoachService aiCoachService) {
        this.aiCoachService = aiCoachService;
    }

    /**
     * POST /api/ai/plans/generate
     * Generate a personalized training plan based on initial assessment.
     * Max response time: 5 seconds.
     * Adapted to progress, age, and medical conditions.
     * Req 3.1, 3.2, 3.7, 3.8, 3.9
     */
    @PostMapping("/plans/generate")
    public ResponseEntity<GeneratedPlanResponse> generatePlan(
            @Valid @RequestBody GeneratePlanRequest request) {
        GeneratedPlanResponse response = aiCoachService.generatePlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/ai/recommendations
     * Recommend an exercise based on user profile and criteria.
     * Max response time: 500ms.
     * Req 3.4
     */
    @PostMapping("/recommendations")
    public ResponseEntity<ExerciseRecommendationResponse> recommendExercise(
            @Valid @RequestBody ExerciseRecommendationRequest request) {
        ExerciseRecommendationResponse response = aiCoachService.recommendExercise(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/ai/alternatives
     * Find alternative exercises when specific equipment is not available.
     * Alternatives must target the same muscle groups.
     * Req 3.6
     */
    @PostMapping("/alternatives")
    public ResponseEntity<AlternativeExerciseResponse> findAlternatives(
            @Valid @RequestBody AlternativeExerciseRequest request) {
        AlternativeExerciseResponse response = aiCoachService.findAlternatives(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/ai/warmup
     * Recommend warmup exercises based on planned exercise type.
     * Includes analysis of whether warmup is recommended.
     * Req 3.5
     */
    @PostMapping("/warmup")
    public ResponseEntity<WarmupResponse> recommendWarmup(
            @Valid @RequestBody WarmupRequest request) {
        WarmupResponse response = aiCoachService.recommendWarmup(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/ai/overtraining/check
     * Analyze biometric data (resting heart rate, heart rate variability, sleep quality)
     * and generate rest alert when overtraining is detected.
     * Req 3.3, 18.3
     */
    @PostMapping("/overtraining/check")
    public ResponseEntity<OvertrainingCheckResponse> checkOvertraining(
            @Valid @RequestBody OvertrainingCheckRequest request) {
        OvertrainingCheckResponse response = aiCoachService.checkOvertraining(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/ai/adherence/predict
     * Predict adherence to training plan using ML model (target accuracy > 85%).
     * Req 3.10, 18.1
     */
    @PostMapping("/adherence/predict")
    public ResponseEntity<AdherencePredictionResponse> predictAdherence(
            @Valid @RequestBody AdherencePredictionRequest request) {
        AdherencePredictionResponse response = aiCoachService.predictAdherence(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/ai/feedback
     * Record user feedback on AI recommendations to retrain ML model.
     * Req 18.6
     */
    @PostMapping("/feedback")
    public ResponseEntity<RecommendationFeedbackResponse> recordFeedback(
            @Valid @RequestBody RecommendationFeedbackRequest request) {
        RecommendationFeedbackResponse response = aiCoachService.recordFeedback(request);
        return ResponseEntity.ok(response);
    }
}
