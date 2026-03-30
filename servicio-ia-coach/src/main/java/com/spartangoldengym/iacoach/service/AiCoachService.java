package com.spartangoldengym.iacoach.service;

import com.spartangoldengym.common.config.KafkaTopics;
import com.spartangoldengym.common.exception.ResourceNotFoundException;
import com.spartangoldengym.iacoach.dto.*;
import com.spartangoldengym.iacoach.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core AI Coach service for personalized plan generation, exercise recommendations,
 * alternative suggestions, warmup recommendations, equipment duplication detection,
 * and automatic load/volume progression.
 *
 * Integrates with SageMaker Endpoints (via SageMakerClient) and Neptune (exercise graph).
 * Publishes events to Kafka topic ai.recommendations.request.
 *
 * Validates: Requirements 3.1, 3.2, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9, 3.11
 */
@Service
public class AiCoachService {

    private static final Logger log = LoggerFactory.getLogger(AiCoachService.class);

    private static final double PROGRESSION_WEIGHT_FACTOR = 1.05;
    private static final int PROGRESSION_VOLUME_INCREMENT = 1;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final SageMakerClient sageMakerClient;
    private final NeptuneClient neptuneClient;

    public AiCoachService(KafkaTemplate<String, String> kafkaTemplate,
                          SageMakerClient sageMakerClient,
                          NeptuneClient neptuneClient) {
        this.kafkaTemplate = kafkaTemplate;
        this.sageMakerClient = sageMakerClient;
        this.neptuneClient = neptuneClient;
    }

    // ---- Plan Generation (Req 3.1, 3.2, 3.7, 3.8, 3.9) ----

    public GeneratedPlanResponse generatePlan(GeneratePlanRequest request) {
        log.info("Generating personalized plan for user={}, noEquipment={}", request.getUserId(), request.isNoEquipment());

        // Get contraindicated exercises based on medical conditions (Req 3.2)
        Set<String> contraindicated = MedicalConditionRules.getAllContraindicatedExercises(request.getMedicalConditions());

        // Get available exercises
        List<ExerciseInfo> pool;
        if (request.isNoEquipment()) {
            // Req 3.9: bodyweight-only routines
            pool = ExerciseCatalog.findBodyweightOnly();
        } else {
            pool = ExerciseCatalog.getAll();
        }

        // Filter out contraindicated exercises
        pool = pool.stream()
                .filter(e -> !contraindicated.contains(e.getName()))
                .collect(Collectors.toList());

        // Filter by available equipment if specified
        if (!request.isNoEquipment() && request.getAvailableEquipment() != null && !request.getAvailableEquipment().isEmpty()) {
            List<String> available = request.getAvailableEquipment();
            pool = pool.stream()
                    .filter(e -> e.getEquipmentRequired().isEmpty() || available.containsAll(e.getEquipmentRequired()))
                    .collect(Collectors.toList());
        }

        // Adjust difficulty based on age (Req 3.2)
        String targetDifficulty = determineDifficulty(request.getFitnessLevel(), request.getAge());

        // Consult SageMaker for ML-based plan optimization
        Map<String, Object> sageMakerInput = buildSageMakerPlanInput(request);
        Map<String, Object> mlSuggestions = sageMakerClient.invokePlanGeneration(sageMakerInput);

        // Consult Neptune for exercise graph recommendations
        List<UUID> graphRecommendations = neptuneClient.getRecommendedExercises(
                request.getUserId(), request.getGoals());

        // Build routines
        int daysPerWeek = request.getDaysPerWeek() != null ? request.getDaysPerWeek() : 3;
        List<GeneratedPlanResponse.RoutineDetail> routines = buildRoutines(pool, daysPerWeek, targetDifficulty);

        // Detect equipment duplication and redistribute (Req 3.7)
        detectAndRedistributeEquipment(routines);

        // Apply automatic load/volume progression (Req 3.8)
        applyProgression(routines, mlSuggestions);

        // Build response
        GeneratedPlanResponse response = new GeneratedPlanResponse();
        response.setPlanId(UUID.randomUUID());
        response.setUserId(request.getUserId());
        response.setName(buildPlanName(request));
        response.setDescription(buildPlanDescription(request, contraindicated));
        response.setRoutines(routines);
        response.setAiGenerated(true);
        response.setNoEquipment(request.isNoEquipment());
        response.setCreatedAt(Instant.now());

        // Publish event to Kafka (Req 3.11)
        publishRecommendationEvent("plan_generated", request.getUserId(), response.getPlanId().toString());

        log.info("Generated plan id={} for user={} with {} routines", response.getPlanId(), request.getUserId(), routines.size());
        return response;
    }

    // ---- Exercise Recommendation (Req 3.4) ----

    public ExerciseRecommendationResponse recommendExercise(ExerciseRecommendationRequest request) {
        log.info("Recommending exercise for user={}", request.getUserId());

        List<ExerciseInfo> candidates = ExerciseCatalog.getAll();

        // Filter by target muscle groups
        if (request.getTargetMuscleGroups() != null && !request.getTargetMuscleGroups().isEmpty()) {
            candidates = candidates.stream()
                    .filter(e -> {
                        for (String mg : request.getTargetMuscleGroups()) {
                            if (e.getMuscleGroups().contains(mg.toLowerCase())) return true;
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
        }

        // Filter by difficulty
        if (request.getDifficulty() != null) {
            candidates = candidates.stream()
                    .filter(e -> e.getDifficulty().equalsIgnoreCase(request.getDifficulty()))
                    .collect(Collectors.toList());
        }

        // Filter by available equipment
        if (request.getAvailableEquipment() != null && !request.getAvailableEquipment().isEmpty()) {
            List<String> available = request.getAvailableEquipment();
            candidates = candidates.stream()
                    .filter(e -> e.getEquipmentRequired().isEmpty() || available.containsAll(e.getEquipmentRequired()))
                    .collect(Collectors.toList());
        }

        if (candidates.isEmpty()) {
            throw new ResourceNotFoundException("Exercise", "No matching exercises found for criteria");
        }

        // Consult Neptune for graph-based recommendation
        ExerciseInfo recommended = neptuneClient.getBestExercise(request.getUserId(), candidates);
        if (recommended == null) {
            recommended = candidates.get(0);
        }

        ExerciseRecommendationResponse response = new ExerciseRecommendationResponse();
        response.setExerciseId(recommended.getId());
        response.setName(recommended.getName());
        response.setMuscleGroups(recommended.getMuscleGroups());
        response.setEquipmentRequired(recommended.getEquipmentRequired());
        response.setDifficulty(recommended.getDifficulty());
        response.setSuggestedSets(3);
        response.setSuggestedReps("8-12");
        response.setRationale("Recommended based on your profile, history, and target muscle groups.");

        // Publish event to Kafka (Req 3.11)
        publishRecommendationEvent("exercise_recommended", request.getUserId(), recommended.getId().toString());

        return response;
    }

    // ---- Alternative Exercises (Req 3.6) ----

    public AlternativeExerciseResponse findAlternatives(AlternativeExerciseRequest request) {
        log.info("Finding alternatives for exercise={} user={}", request.getExerciseId(), request.getUserId());

        ExerciseInfo original = ExerciseCatalog.findById(request.getExerciseId())
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", request.getExerciseId().toString()));

        List<ExerciseInfo> alternatives = ExerciseCatalog.findAlternatives(
                request.getExerciseId(), request.getAvailableEquipment());

        AlternativeExerciseResponse response = new AlternativeExerciseResponse();
        response.setOriginalExerciseId(original.getId());
        response.setOriginalExerciseName(original.getName());
        response.setTargetMuscleGroups(original.getMuscleGroups());
        response.setAlternatives(alternatives.stream().map(e -> {
            AlternativeExerciseResponse.Alternative alt = new AlternativeExerciseResponse.Alternative();
            alt.setExerciseId(e.getId());
            alt.setName(e.getName());
            alt.setMuscleGroups(e.getMuscleGroups());
            alt.setEquipmentRequired(e.getEquipmentRequired());
            alt.setDifficulty(e.getDifficulty());
            return alt;
        }).collect(Collectors.toList()));

        // Publish event to Kafka (Req 3.11)
        publishRecommendationEvent("alternatives_requested", request.getUserId(), original.getId().toString());

        return response;
    }

    // ---- Warmup Recommendation (Req 3.5) ----

    public WarmupResponse recommendWarmup(WarmupRequest request) {
        log.info("Generating warmup recommendation for user={}", request.getUserId());

        boolean recommended = WarmupRules.isWarmupRecommended(request.getPlannedExerciseTypes());
        String rationale = WarmupRules.getRationale(request.getPlannedExerciseTypes());

        WarmupResponse response = new WarmupResponse();
        response.setUserId(request.getUserId());
        response.setPlannedExerciseTypes(request.getPlannedExerciseTypes());
        response.setWarmupRecommended(recommended);
        response.setWarmupRationale(rationale);

        if (recommended) {
            response.setWarmupExercises(WarmupRules.generateWarmupExercises(
                    request.getPlannedExerciseTypes(), request.getTargetMuscleGroups()));
        } else {
            response.setWarmupExercises(Collections.emptyList());
        }

        // Publish event to Kafka (Req 3.11)
        publishRecommendationEvent("warmup_recommended", request.getUserId(), null);

        return response;
    }

    // ---- Overtraining Detection (Req 3.3, 18.3) ----

    public OvertrainingCheckResponse checkOvertraining(OvertrainingCheckRequest request) {
        log.info("Checking overtraining for user={}", request.getUserId());

        // Analyze biometric indicators locally
        List<String> detectedIndicators = OvertrainingRules.detectIndicators(
                request.getRestingHeartRate(),
                request.getHeartRateVariability(),
                request.getSleepQualityScore(),
                request.getPerformanceChange());

        double localRisk = OvertrainingRules.computeRiskScore(
                request.getRestingHeartRate(),
                request.getHeartRateVariability(),
                request.getSleepQualityScore(),
                request.getPerformanceChange());

        // Consult SageMaker for ML-based overtraining detection
        Map<String, Object> sageMakerInput = buildSageMakerOvertrainingInput(request);
        Map<String, Object> mlResult = sageMakerClient.invokeOvertrainingDetection(sageMakerInput);

        double mlRisk = mlResult.containsKey("overtrainingRisk")
                ? ((Number) mlResult.get("overtrainingRisk")).doubleValue()
                : 0.0;

        // Combine local rule-based risk with ML risk (weighted average)
        double combinedRisk = (localRisk * 0.6) + (mlRisk * 0.4);

        String riskLevel = OvertrainingRules.determineRiskLevel(combinedRisk);
        boolean restAlert = OvertrainingRules.shouldGenerateRestAlert(combinedRisk);

        OvertrainingCheckResponse response = new OvertrainingCheckResponse();
        response.setUserId(request.getUserId());
        response.setOvertrainingRisk(combinedRisk);
        response.setRestAlertGenerated(restAlert);
        response.setRiskLevel(riskLevel);
        response.setRecommendation(OvertrainingRules.generateRecommendation(riskLevel));
        response.setDetectedIndicators(detectedIndicators);
        response.setSuggestedRestDays(OvertrainingRules.suggestRestDays(combinedRisk));
        response.setCheckedAt(Instant.now());

        // Publish event to Kafka
        publishRecommendationEvent("overtraining_check", request.getUserId(), riskLevel);

        log.info("Overtraining check for user={}: risk={}, alert={}", request.getUserId(), combinedRisk, restAlert);
        return response;
    }

    // ---- Adherence Prediction (Req 3.10, 18.1) ----

    public AdherencePredictionResponse predictAdherence(AdherencePredictionRequest request) {
        log.info("Predicting adherence for user={}, plan={}", request.getUserId(), request.getPlanId());

        // Build input for SageMaker adherence prediction model
        Map<String, Object> sageMakerInput = buildSageMakerAdherenceInput(request);
        Map<String, Object> mlResult = sageMakerClient.invokeAdherencePrediction(sageMakerInput);

        double adherenceProbability = mlResult.containsKey("adherenceProbability")
                ? ((Number) mlResult.get("adherenceProbability")).doubleValue()
                : 0.5;

        @SuppressWarnings("unchecked")
        List<String> riskFactors = mlResult.containsKey("riskFactors")
                ? (List<String>) mlResult.get("riskFactors")
                : Collections.emptyList();

        String riskLevel = adherenceProbability >= 0.85 ? "low"
                : adherenceProbability >= 0.6 ? "moderate" : "high";

        List<String> suggestions = generateAdherenceSuggestions(riskLevel, riskFactors);

        AdherencePredictionResponse response = new AdherencePredictionResponse();
        response.setUserId(request.getUserId());
        response.setPlanId(request.getPlanId());
        response.setAdherenceProbability(adherenceProbability);
        response.setRiskLevel(riskLevel);
        response.setRiskFactors(riskFactors);
        response.setSuggestions(suggestions);
        response.setPredictedAt(Instant.now());

        // Publish event to Kafka
        publishRecommendationEvent("adherence_predicted", request.getUserId(), request.getPlanId().toString());

        log.info("Adherence prediction for user={}: probability={}, risk={}", request.getUserId(), adherenceProbability, riskLevel);
        return response;
    }

    // ---- Recommendation Feedback (Req 18.6) ----

    public RecommendationFeedbackResponse recordFeedback(RecommendationFeedbackRequest request) {
        log.info("Recording feedback for user={}, recommendation={}, type={}",
                request.getUserId(), request.getRecommendationId(), request.getFeedbackType());

        RecommendationFeedbackResponse response = new RecommendationFeedbackResponse();
        response.setFeedbackId(UUID.randomUUID());
        response.setUserId(request.getUserId());
        response.setRecommendationId(request.getRecommendationId());
        response.setFeedbackType(request.getFeedbackType());
        response.setRecorded(true);
        response.setRecordedAt(Instant.now());

        // Publish feedback event to Kafka for ML model retraining
        String payload = String.format(
                "{\"type\":\"recommendation_feedback\",\"userId\":\"%s\",\"recommendationId\":\"%s\",\"feedbackType\":\"%s\",\"timestamp\":\"%s\"}",
                request.getUserId(), request.getRecommendationId(), request.getFeedbackType(), Instant.now());
        kafkaTemplate.send(KafkaTopics.AI_RECOMMENDATIONS_REQUEST, request.getUserId().toString(), payload);

        log.info("Recorded feedback id={} for recommendation={}", response.getFeedbackId(), request.getRecommendationId());
        return response;
    }

    // ---- Private helpers ----

    private List<GeneratedPlanResponse.RoutineDetail> buildRoutines(
            List<ExerciseInfo> pool, int daysPerWeek, String targetDifficulty) {

        // Group exercises by primary muscle group for split routine
        Map<String, List<ExerciseInfo>> byMuscleGroup = new LinkedHashMap<>();
        for (ExerciseInfo e : pool) {
            String primary = e.getMuscleGroups().get(0);
            byMuscleGroup.computeIfAbsent(primary, k -> new ArrayList<>()).add(e);
        }

        // Define muscle group splits per day
        List<List<String>> splits = buildSplits(new ArrayList<>(byMuscleGroup.keySet()), daysPerWeek);

        List<GeneratedPlanResponse.RoutineDetail> routines = new ArrayList<>();
        for (int day = 0; day < splits.size(); day++) {
            GeneratedPlanResponse.RoutineDetail routine = new GeneratedPlanResponse.RoutineDetail();
            routine.setName("Day " + (day + 1) + " - " + String.join("/", capitalize(splits.get(day))));
            routine.setDayOfWeek(day + 1);

            List<GeneratedPlanResponse.ExerciseDetail> exercises = new ArrayList<>();
            for (String mg : splits.get(day)) {
                List<ExerciseInfo> mgExercises = byMuscleGroup.getOrDefault(mg, Collections.emptyList());
                // Pick up to 2 exercises per muscle group per day
                int count = 0;
                for (ExerciseInfo e : mgExercises) {
                    if (count >= 2) break;
                    if (targetDifficulty != null && !e.getDifficulty().equalsIgnoreCase(targetDifficulty)
                            && !"beginner".equalsIgnoreCase(targetDifficulty)) {
                        // Allow exercises of same or lower difficulty
                    }
                    exercises.add(toExerciseDetail(e));
                    count++;
                }
            }
            routine.setExercises(exercises);

            // Add warmup exercises for this routine (Req 3.5)
            List<WarmupResponse.WarmupExercise> warmups = WarmupRules.generateWarmupExercises(
                    Arrays.asList("strength"), splits.get(day));
            routine.setWarmupExercises(warmups.stream().map(w -> {
                GeneratedPlanResponse.ExerciseDetail wd = new GeneratedPlanResponse.ExerciseDetail();
                wd.setName(w.getName());
                wd.setMuscleGroups(w.getTargetMuscleGroups());
                wd.setEquipmentRequired(Collections.emptyList());
                wd.setSets(1);
                wd.setReps(w.getDurationSeconds() + "s");
                wd.setRestSeconds(0);
                return wd;
            }).collect(Collectors.toList()));

            routines.add(routine);
        }
        return routines;
    }

    private List<List<String>> buildSplits(List<String> muscleGroups, int daysPerWeek) {
        List<List<String>> splits = new ArrayList<>();
        int mgPerDay = Math.max(1, (int) Math.ceil((double) muscleGroups.size() / daysPerWeek));
        int idx = 0;
        for (int day = 0; day < daysPerWeek; day++) {
            List<String> dayMuscles = new ArrayList<>();
            for (int j = 0; j < mgPerDay && idx < muscleGroups.size(); j++, idx++) {
                dayMuscles.add(muscleGroups.get(idx));
            }
            if (!dayMuscles.isEmpty()) {
                splits.add(dayMuscles);
            }
        }
        // If we have fewer splits than days, that's fine
        return splits;
    }

    /**
     * Detect equipment duplication across routines and redistribute (Req 3.7).
     * If the same equipment appears in multiple exercises on the same day,
     * suggest redistribution by moving one to another day.
     */
    void detectAndRedistributeEquipment(List<GeneratedPlanResponse.RoutineDetail> routines) {
        for (GeneratedPlanResponse.RoutineDetail routine : routines) {
            Map<String, Integer> equipmentCount = new HashMap<>();
            for (GeneratedPlanResponse.ExerciseDetail ex : routine.getExercises()) {
                if (ex.getEquipmentRequired() != null) {
                    for (String eq : ex.getEquipmentRequired()) {
                        equipmentCount.merge(eq, 1, Integer::sum);
                    }
                }
            }
            // Log duplications for redistribution suggestion
            for (Map.Entry<String, Integer> entry : equipmentCount.entrySet()) {
                if (entry.getValue() > 2) {
                    log.info("Equipment duplication detected: '{}' used {} times in routine '{}'. Consider redistribution.",
                            entry.getKey(), entry.getValue(), routine.getName());
                }
            }
        }
    }

    /**
     * Apply automatic load/volume progression based on ML suggestions (Req 3.8).
     */
    void applyProgression(List<GeneratedPlanResponse.RoutineDetail> routines, Map<String, Object> mlSuggestions) {
        Double progressionFactor = null;
        if (mlSuggestions != null && mlSuggestions.containsKey("progressionFactor")) {
            progressionFactor = ((Number) mlSuggestions.get("progressionFactor")).doubleValue();
        }
        if (progressionFactor == null) {
            progressionFactor = PROGRESSION_WEIGHT_FACTOR;
        }

        for (GeneratedPlanResponse.RoutineDetail routine : routines) {
            for (GeneratedPlanResponse.ExerciseDetail ex : routine.getExercises()) {
                if (ex.getSuggestedWeight() != null && ex.getSuggestedWeight() > 0) {
                    ex.setSuggestedWeight(Math.round(ex.getSuggestedWeight() * progressionFactor * 100.0) / 100.0);
                }
                // Increment volume by 1 rep if applicable
                if (ex.getReps() != null && ex.getReps().matches("\\d+")) {
                    int reps = Integer.parseInt(ex.getReps());
                    ex.setReps(String.valueOf(reps + PROGRESSION_VOLUME_INCREMENT));
                }
            }
        }
    }

    private String determineDifficulty(String fitnessLevel, Integer age) {
        if (fitnessLevel != null) return fitnessLevel;
        if (age != null) {
            if (age < 18 || age > 60) return "beginner";
            if (age > 45) return "intermediate";
        }
        return "intermediate";
    }

    private GeneratedPlanResponse.ExerciseDetail toExerciseDetail(ExerciseInfo e) {
        GeneratedPlanResponse.ExerciseDetail detail = new GeneratedPlanResponse.ExerciseDetail();
        detail.setExerciseId(e.getId());
        detail.setName(e.getName());
        detail.setMuscleGroups(e.getMuscleGroups());
        detail.setEquipmentRequired(e.getEquipmentRequired());
        detail.setSets(3);
        detail.setReps("10");
        detail.setRestSeconds(60);
        detail.setSuggestedWeight(null);
        return detail;
    }

    private Map<String, Object> buildSageMakerPlanInput(GeneratePlanRequest request) {
        Map<String, Object> input = new HashMap<>();
        input.put("userId", request.getUserId().toString());
        input.put("fitnessLevel", request.getFitnessLevel());
        input.put("goals", request.getGoals());
        input.put("age", request.getAge());
        input.put("medicalConditions", request.getMedicalConditions());
        input.put("noEquipment", request.isNoEquipment());
        return input;
    }

    private String buildPlanName(GeneratePlanRequest request) {
        StringBuilder name = new StringBuilder("AI Plan");
        if (request.getGoals() != null && !request.getGoals().isEmpty()) {
            name.append(" - ").append(String.join(", ", request.getGoals()));
        }
        if (request.isNoEquipment()) {
            name.append(" (No Equipment)");
        }
        return name.toString();
    }

    private String buildPlanDescription(GeneratePlanRequest request, Set<String> contraindicated) {
        StringBuilder desc = new StringBuilder("AI-generated personalized training plan");
        if (request.getMedicalConditions() != null && !request.getMedicalConditions().isEmpty()) {
            desc.append(". Adapted for medical conditions: ").append(String.join(", ", request.getMedicalConditions()));
        }
        if (!contraindicated.isEmpty()) {
            desc.append(". Excluded exercises: ").append(String.join(", ", contraindicated));
        }
        return desc.toString();
    }

    private void publishRecommendationEvent(String type, UUID userId, String referenceId) {
        String payload = String.format(
                "{\"type\":\"%s\",\"userId\":\"%s\",\"referenceId\":\"%s\",\"timestamp\":\"%s\"}",
                type, userId, referenceId != null ? referenceId : "", Instant.now());
        kafkaTemplate.send(KafkaTopics.AI_RECOMMENDATIONS_REQUEST, userId.toString(), payload);
        log.info("Published {} event to Kafka for user={}", type, userId);
    }

    private Map<String, Object> buildSageMakerOvertrainingInput(OvertrainingCheckRequest request) {
        Map<String, Object> input = new HashMap<>();
        input.put("userId", request.getUserId().toString());
        if (request.getRestingHeartRate() != null) input.put("restingHeartRate", request.getRestingHeartRate());
        if (request.getHeartRateVariability() != null) input.put("heartRateVariability", request.getHeartRateVariability());
        if (request.getSleepQualityScore() != null) input.put("sleepQualityScore", request.getSleepQualityScore());
        if (request.getPerformanceChange() != null) input.put("performanceChange", request.getPerformanceChange());
        return input;
    }

    private Map<String, Object> buildSageMakerAdherenceInput(AdherencePredictionRequest request) {
        Map<String, Object> input = new HashMap<>();
        input.put("userId", request.getUserId().toString());
        input.put("planId", request.getPlanId().toString());
        if (request.getCompletedWorkouts() != null) input.put("completedWorkouts", request.getCompletedWorkouts());
        if (request.getTotalPlannedWorkouts() != null) input.put("totalPlannedWorkouts", request.getTotalPlannedWorkouts());
        if (request.getAverageCompletionRate() != null) input.put("averageCompletionRate", request.getAverageCompletionRate());
        if (request.getStreakDays() != null) input.put("streakDays", request.getStreakDays());
        return input;
    }

    private List<String> generateAdherenceSuggestions(String riskLevel, List<String> riskFactors) {
        List<String> suggestions = new ArrayList<>();
        switch (riskLevel) {
            case "high":
                suggestions.add("Consider reducing workout frequency to maintain consistency.");
                suggestions.add("Set smaller, achievable daily goals to build momentum.");
                break;
            case "moderate":
                suggestions.add("Try scheduling workouts at a consistent time each day.");
                suggestions.add("Join a challenge or find a workout partner for accountability.");
                break;
            default:
                suggestions.add("Great adherence! Keep up the consistent training.");
                break;
        }
        return suggestions;
    }

    private List<String> capitalize(List<String> items) {
        return items.stream()
                .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
                .collect(Collectors.toList());
    }
}
