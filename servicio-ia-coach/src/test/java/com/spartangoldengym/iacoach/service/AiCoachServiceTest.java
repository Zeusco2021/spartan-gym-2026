package com.spartangoldengym.iacoach.service;

import com.spartangoldengym.iacoach.dto.*;
import com.spartangoldengym.iacoach.model.ExerciseCatalog;
import com.spartangoldengym.iacoach.model.ExerciseInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AiCoachServiceTest {

    private KafkaTemplate<String, String> kafkaTemplate;
    private SageMakerClient sageMakerClient;
    private NeptuneClient neptuneClient;
    private AiCoachService service;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        sageMakerClient = mock(SageMakerClient.class);
        neptuneClient = mock(NeptuneClient.class);
        service = new AiCoachService(kafkaTemplate, sageMakerClient, neptuneClient);

        when(sageMakerClient.invokePlanGeneration(any()))
                .thenReturn(Collections.singletonMap("progressionFactor", 1.05));
        when(neptuneClient.getRecommendedExercises(any(), any()))
                .thenReturn(Collections.emptyList());
        when(neptuneClient.getBestExercise(any(), anyList()))
                .thenAnswer(inv -> {
                    List<ExerciseInfo> candidates = inv.getArgument(1);
                    return candidates.isEmpty() ? null : candidates.get(0);
                });
    }

    // ---- Plan Generation Tests ----

    @Test
    void generatePlan_basicRequest_returnsAiGeneratedPlan() {
        GeneratePlanRequest request = new GeneratePlanRequest();
        request.setUserId(UUID.randomUUID());
        request.setFitnessLevel("beginner");
        request.setGoals(Arrays.asList("muscle_gain"));
        request.setDaysPerWeek(3);

        GeneratedPlanResponse response = service.generatePlan(request);

        assertNotNull(response.getPlanId());
        assertEquals(request.getUserId(), response.getUserId());
        assertTrue(response.isAiGenerated());
        assertFalse(response.getRoutines().isEmpty());
        verify(kafkaTemplate).send(eq("ai.recommendations.request"), anyString(), anyString());
    }

    @Test
    void generatePlan_withMedicalConditions_excludesContraindicatedExercises() {
        GeneratePlanRequest request = new GeneratePlanRequest();
        request.setUserId(UUID.randomUUID());
        request.setMedicalConditions(Arrays.asList("back_problems"));
        request.setDaysPerWeek(3);

        GeneratedPlanResponse response = service.generatePlan(request);

        // Verify no contraindicated exercises are included
        Set<String> contraindicated = new HashSet<>(Arrays.asList(
                "Barbell Squat", "Romanian Deadlift", "Bent-over Row", "Overhead Press", "Ab Rollout"));
        for (GeneratedPlanResponse.RoutineDetail routine : response.getRoutines()) {
            for (GeneratedPlanResponse.ExerciseDetail ex : routine.getExercises()) {
                assertFalse(contraindicated.contains(ex.getName()),
                        "Contraindicated exercise found: " + ex.getName());
            }
        }
    }

    @Test
    void generatePlan_noEquipment_allExercisesBodyweightOnly() {
        GeneratePlanRequest request = new GeneratePlanRequest();
        request.setUserId(UUID.randomUUID());
        request.setNoEquipment(true);
        request.setDaysPerWeek(3);

        GeneratedPlanResponse response = service.generatePlan(request);

        assertTrue(response.isNoEquipment());
        for (GeneratedPlanResponse.RoutineDetail routine : response.getRoutines()) {
            for (GeneratedPlanResponse.ExerciseDetail ex : routine.getExercises()) {
                assertTrue(ex.getEquipmentRequired() == null || ex.getEquipmentRequired().isEmpty(),
                        "Exercise requires equipment: " + ex.getName());
            }
        }
    }

    @Test
    void generatePlan_includesWarmupExercises() {
        GeneratePlanRequest request = new GeneratePlanRequest();
        request.setUserId(UUID.randomUUID());
        request.setDaysPerWeek(2);

        GeneratedPlanResponse response = service.generatePlan(request);

        for (GeneratedPlanResponse.RoutineDetail routine : response.getRoutines()) {
            assertNotNull(routine.getWarmupExercises());
            assertFalse(routine.getWarmupExercises().isEmpty(),
                    "Routine should include warmup exercises");
        }
    }

    @Test
    void generatePlan_publishesKafkaEvent() {
        GeneratePlanRequest request = new GeneratePlanRequest();
        request.setUserId(UUID.randomUUID());
        request.setDaysPerWeek(3);

        service.generatePlan(request);

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("ai.recommendations.request"), anyString(), payloadCaptor.capture());
        assertTrue(payloadCaptor.getValue().contains("plan_generated"));
    }

    // ---- Exercise Recommendation Tests ----

    @Test
    void recommendExercise_byMuscleGroup_returnsMatchingExercise() {
        ExerciseRecommendationRequest request = new ExerciseRecommendationRequest();
        request.setUserId(UUID.randomUUID());
        request.setTargetMuscleGroups(Arrays.asList("chest"));

        ExerciseRecommendationResponse response = service.recommendExercise(request);

        assertNotNull(response.getExerciseId());
        assertTrue(response.getMuscleGroups().contains("chest"));
        verify(kafkaTemplate).send(eq("ai.recommendations.request"), anyString(), anyString());
    }

    @Test
    void recommendExercise_byDifficulty_returnsMatchingDifficulty() {
        ExerciseRecommendationRequest request = new ExerciseRecommendationRequest();
        request.setUserId(UUID.randomUUID());
        request.setDifficulty("beginner");

        ExerciseRecommendationResponse response = service.recommendExercise(request);

        assertEquals("beginner", response.getDifficulty());
    }

    // ---- Alternative Exercises Tests ----

    @Test
    void findAlternatives_returnsExercisesWithSameMuscleGroups() {
        // Get Bench Press ID
        ExerciseInfo benchPress = ExerciseCatalog.findByName("Bench Press").orElseThrow(
                () -> new RuntimeException("Bench Press not found"));

        AlternativeExerciseRequest request = new AlternativeExerciseRequest();
        request.setExerciseId(benchPress.getId());
        request.setUserId(UUID.randomUUID());

        AlternativeExerciseResponse response = service.findAlternatives(request);

        assertEquals(benchPress.getId(), response.getOriginalExerciseId());
        assertFalse(response.getAlternatives().isEmpty());
        // All alternatives should share at least one muscle group with bench press
        for (AlternativeExerciseResponse.Alternative alt : response.getAlternatives()) {
            boolean hasOverlap = false;
            for (String mg : alt.getMuscleGroups()) {
                if (response.getTargetMuscleGroups().contains(mg)) {
                    hasOverlap = true;
                    break;
                }
            }
            assertTrue(hasOverlap, "Alternative " + alt.getName() + " should target same muscle groups");
        }
    }

    @Test
    void findAlternatives_withEquipmentFilter_respectsAvailableEquipment() {
        ExerciseInfo benchPress = ExerciseCatalog.findByName("Bench Press").orElseThrow(
                () -> new RuntimeException("Bench Press not found"));

        AlternativeExerciseRequest request = new AlternativeExerciseRequest();
        request.setExerciseId(benchPress.getId());
        request.setUserId(UUID.randomUUID());
        request.setAvailableEquipment(Collections.emptyList()); // No equipment

        AlternativeExerciseResponse response = service.findAlternatives(request);

        for (AlternativeExerciseResponse.Alternative alt : response.getAlternatives()) {
            assertTrue(alt.getEquipmentRequired() == null || alt.getEquipmentRequired().isEmpty(),
                    "Alternative " + alt.getName() + " should not require equipment");
        }
    }

    // ---- Warmup Tests ----

    @Test
    void recommendWarmup_strengthExercises_warmupRecommended() {
        WarmupRequest request = new WarmupRequest();
        request.setUserId(UUID.randomUUID());
        request.setPlannedExerciseTypes(Arrays.asList("strength"));
        request.setTargetMuscleGroups(Arrays.asList("chest", "back"));

        WarmupResponse response = service.recommendWarmup(request);

        assertTrue(response.isWarmupRecommended());
        assertFalse(response.getWarmupExercises().isEmpty());
    }

    @Test
    void recommendWarmup_lightCardio_warmupOptional() {
        WarmupRequest request = new WarmupRequest();
        request.setUserId(UUID.randomUUID());
        request.setPlannedExerciseTypes(Arrays.asList("stretching"));

        WarmupResponse response = service.recommendWarmup(request);

        assertFalse(response.isWarmupRecommended());
    }

    // ---- Overtraining Detection Tests (Req 3.3, 18.3) ----

    @Test
    void checkOvertraining_highRiskIndicators_generatesRestAlert() {
        OvertrainingCheckRequest request = new OvertrainingCheckRequest();
        request.setUserId(UUID.randomUUID());
        request.setRestingHeartRate(90.0);
        request.setHeartRateVariability(15.0);
        request.setSleepQualityScore(25.0);
        request.setPerformanceChange(-25.0);

        when(sageMakerClient.invokeOvertrainingDetection(any()))
                .thenReturn(Collections.singletonMap("overtrainingRisk", 0.8));

        OvertrainingCheckResponse response = service.checkOvertraining(request);

        assertNotNull(response);
        assertEquals(request.getUserId(), response.getUserId());
        assertTrue(response.isRestAlertGenerated());
        assertEquals("high", response.getRiskLevel());
        assertTrue(response.getOvertrainingRisk() > 0.0);
        assertFalse(response.getDetectedIndicators().isEmpty());
        assertTrue(response.getSuggestedRestDays() > 0);
        assertNotNull(response.getRecommendation());
        assertNotNull(response.getCheckedAt());
        verify(kafkaTemplate).send(eq("ai.recommendations.request"), anyString(), anyString());
    }

    @Test
    void checkOvertraining_lowRiskIndicators_noAlert() {
        OvertrainingCheckRequest request = new OvertrainingCheckRequest();
        request.setUserId(UUID.randomUUID());
        request.setRestingHeartRate(60.0);
        request.setHeartRateVariability(50.0);
        request.setSleepQualityScore(80.0);
        request.setPerformanceChange(5.0);

        when(sageMakerClient.invokeOvertrainingDetection(any()))
                .thenReturn(Collections.singletonMap("overtrainingRisk", 0.1));

        OvertrainingCheckResponse response = service.checkOvertraining(request);

        assertFalse(response.isRestAlertGenerated());
        assertEquals("low", response.getRiskLevel());
        assertEquals(0, response.getSuggestedRestDays());
        assertTrue(response.getDetectedIndicators().isEmpty());
    }

    @Test
    void checkOvertraining_noData_returnsLowRisk() {
        OvertrainingCheckRequest request = new OvertrainingCheckRequest();
        request.setUserId(UUID.randomUUID());

        when(sageMakerClient.invokeOvertrainingDetection(any()))
                .thenReturn(Collections.singletonMap("overtrainingRisk", 0.0));

        OvertrainingCheckResponse response = service.checkOvertraining(request);

        assertEquals("low", response.getRiskLevel());
        assertFalse(response.isRestAlertGenerated());
    }

    // ---- Adherence Prediction Tests (Req 3.10, 18.1) ----

    @Test
    void predictAdherence_highAdherence_returnsLowRisk() {
        AdherencePredictionRequest request = new AdherencePredictionRequest();
        request.setUserId(UUID.randomUUID());
        request.setPlanId(UUID.randomUUID());
        request.setCompletedWorkouts(20);
        request.setTotalPlannedWorkouts(24);
        request.setStreakDays(14);

        Map<String, Object> mlResult = new HashMap<>();
        mlResult.put("adherenceProbability", 0.92);
        mlResult.put("riskFactors", Collections.emptyList());
        when(sageMakerClient.invokeAdherencePrediction(any())).thenReturn(mlResult);

        AdherencePredictionResponse response = service.predictAdherence(request);

        assertNotNull(response);
        assertEquals(request.getUserId(), response.getUserId());
        assertEquals(request.getPlanId(), response.getPlanId());
        assertEquals(0.92, response.getAdherenceProbability(), 0.001);
        assertEquals("low", response.getRiskLevel());
        assertNotNull(response.getSuggestions());
        assertNotNull(response.getPredictedAt());
        verify(kafkaTemplate).send(eq("ai.recommendations.request"), anyString(), anyString());
    }

    @Test
    void predictAdherence_lowAdherence_returnsHighRisk() {
        AdherencePredictionRequest request = new AdherencePredictionRequest();
        request.setUserId(UUID.randomUUID());
        request.setPlanId(UUID.randomUUID());

        Map<String, Object> mlResult = new HashMap<>();
        mlResult.put("adherenceProbability", 0.35);
        mlResult.put("riskFactors", Arrays.asList("low_frequency", "missed_sessions"));
        when(sageMakerClient.invokeAdherencePrediction(any())).thenReturn(mlResult);

        AdherencePredictionResponse response = service.predictAdherence(request);

        assertEquals("high", response.getRiskLevel());
        assertTrue(response.getAdherenceProbability() < 0.6);
        assertFalse(response.getSuggestions().isEmpty());
    }

    // ---- Recommendation Feedback Tests (Req 18.6) ----

    @Test
    void recordFeedback_accepted_recordsSuccessfully() {
        RecommendationFeedbackRequest request = new RecommendationFeedbackRequest();
        request.setUserId(UUID.randomUUID());
        request.setRecommendationId(UUID.randomUUID());
        request.setFeedbackType("accepted");

        RecommendationFeedbackResponse response = service.recordFeedback(request);

        assertNotNull(response.getFeedbackId());
        assertEquals(request.getUserId(), response.getUserId());
        assertEquals(request.getRecommendationId(), response.getRecommendationId());
        assertEquals("accepted", response.getFeedbackType());
        assertTrue(response.isRecorded());
        assertNotNull(response.getRecordedAt());

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("ai.recommendations.request"), anyString(), payloadCaptor.capture());
        assertTrue(payloadCaptor.getValue().contains("recommendation_feedback"));
    }

    @Test
    void recordFeedback_rejected_recordsSuccessfully() {
        RecommendationFeedbackRequest request = new RecommendationFeedbackRequest();
        request.setUserId(UUID.randomUUID());
        request.setRecommendationId(UUID.randomUUID());
        request.setFeedbackType("rejected");
        request.setComment("Too difficult");

        RecommendationFeedbackResponse response = service.recordFeedback(request);

        assertTrue(response.isRecorded());
        assertEquals("rejected", response.getFeedbackType());
    }
}
