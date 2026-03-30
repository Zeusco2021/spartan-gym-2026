package com.spartangoldengym.seguimiento.service;

import com.spartangoldengym.common.config.KafkaTopics;
import com.spartangoldengym.common.exception.ResourceNotFoundException;
import com.spartangoldengym.seguimiento.dto.*;
import com.spartangoldengym.seguimiento.model.WorkoutSession;
import com.spartangoldengym.seguimiento.model.WorkoutSet;
import com.spartangoldengym.seguimiento.repository.WorkoutRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Business logic for workout sessions, sets, heart rate and completion.
 *
 * Validates: Requirements 4.1, 4.2, 4.3, 4.5, 4.6, 4.9, 8.4
 */
@Service
public class WorkoutService {

    private static final Logger log = LoggerFactory.getLogger(WorkoutService.class);

    private final WorkoutRepository workoutRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final TimestreamMetricsService timestreamMetrics;

    public WorkoutService(WorkoutRepository workoutRepository,
                          KafkaTemplate<String, String> kafkaTemplate,
                          TimestreamMetricsService timestreamMetrics) {
        this.workoutRepository = workoutRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.timestreamMetrics = timestreamMetrics;
    }

    /**
     * Start a new workout session. Validates: Req 4.1
     */
    public WorkoutSessionResponse startWorkout(StartWorkoutRequest request) {
        WorkoutSession session = new WorkoutSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setUserId(request.getUserId());
        session.setStartedAt(Instant.now());
        session.setStatus("active");

        workoutRepository.saveSession(session);
        log.info("Workout session started: sessionId={}, userId={}", session.getSessionId(), session.getUserId());
        return WorkoutSessionResponse.fromModel(session);
    }

    /**
     * Record a completed set within a workout session. Validates: Req 4.3
     */
    public WorkoutSetResponse recordSet(String sessionId, RecordSetRequest request) {
        WorkoutSession session = findSessionOrThrow(sessionId);

        WorkoutSet set = new WorkoutSet();
        set.setSessionId(sessionId);
        set.setSetId(UUID.randomUUID().toString());
        set.setExerciseId(request.getExerciseId());
        set.setWeight(request.getWeight());
        set.setReps(request.getReps());
        set.setRestSeconds(request.getRestSeconds());
        set.setTimestamp(Instant.now());

        workoutRepository.saveSet(set);

        // Track unique exercises in the session
        if (!session.getExercises().contains(request.getExerciseId())) {
            session.getExercises().add(request.getExerciseId());
            workoutRepository.saveSession(session);
        }

        log.info("Set recorded: sessionId={}, setId={}, exercise={}, weight={}, reps={}",
                sessionId, set.getSetId(), request.getExerciseId(), request.getWeight(), request.getReps());
        return WorkoutSetResponse.fromModel(set);
    }

    /**
     * Record heart rate from wearable and publish to Kafka. Validates: Req 4.2, 8.4
     * Latency requirement: max 2 seconds.
     */
    public void recordHeartRate(String sessionId, RecordHeartRateRequest request) {
        WorkoutSession session = findSessionOrThrow(sessionId);

        String payload = String.format(
                "{\"sessionId\":\"%s\",\"userId\":\"%s\",\"bpm\":%d,\"deviceType\":\"%s\",\"timestamp\":\"%s\"}",
                sessionId, session.getUserId(), request.getBpm(),
                request.getDeviceType() != null ? request.getDeviceType() : "unknown",
                Instant.now().toString());

        kafkaTemplate.send(KafkaTopics.REAL_TIME_HEARTRATE, session.getUserId(), payload);
        timestreamMetrics.recordHeartRate(session.getUserId(), sessionId, request.getBpm(), request.getDeviceType());

        log.info("Heart rate recorded: sessionId={}, bpm={}", sessionId, request.getBpm());
    }

    /**
     * Complete a workout session, publish to Kafka and store metrics in Timestream. Validates: Req 4.5, 4.9
     */
    public WorkoutSessionResponse completeWorkout(String sessionId) {
        WorkoutSession session = findSessionOrThrow(sessionId);

        Instant now = Instant.now();
        session.setCompletedAt(now);
        session.setStatus("completed");
        session.setTotalDurationSeconds(Duration.between(session.getStartedAt(), now).getSeconds());

        List<WorkoutSet> sets = workoutRepository.findSetsBySessionId(sessionId);
        session.setCaloriesBurned(estimateCalories(session, sets));

        workoutRepository.saveSession(session);

        // Publish workout.completed event to Kafka
        String payload = String.format(
                "{\"sessionId\":\"%s\",\"userId\":\"%s\",\"durationSeconds\":%d,\"exercises\":%d,\"sets\":%d,\"caloriesBurned\":%.1f,\"completedAt\":\"%s\"}",
                sessionId, session.getUserId(), session.getTotalDurationSeconds(),
                session.getExercises().size(), sets.size(), session.getCaloriesBurned(), now.toString());
        kafkaTemplate.send(KafkaTopics.WORKOUT_COMPLETED, session.getUserId(), payload);

        // Store metrics in Timestream
        timestreamMetrics.recordWorkoutMetrics(session, sets);

        log.info("Workout completed: sessionId={}, duration={}s, calories={}",
                sessionId, session.getTotalDurationSeconds(), session.getCaloriesBurned());
        return WorkoutSessionResponse.fromModel(session);
    }

    /**
     * Get workout history for a user. Validates: Req 4.6
     */
    public List<WorkoutSessionResponse> getWorkoutHistory(String userId) {
        return workoutRepository.findSessionsByUserId(userId).stream()
                .map(WorkoutSessionResponse::fromModel)
                .collect(Collectors.toList());
    }

    /**
     * Get aggregated progress metrics for a user. Validates: Req 4.6
     */
    public WorkoutProgressResponse getProgress(String userId) {
        List<WorkoutSession> sessions = workoutRepository.findSessionsByUserId(userId).stream()
                .filter(s -> "completed".equals(s.getStatus()))
                .collect(Collectors.toList());

        WorkoutProgressResponse progress = new WorkoutProgressResponse();
        progress.setUserId(userId);
        progress.setTotalWorkouts(sessions.size());

        long totalDuration = 0;
        double totalCalories = 0;
        double totalVolume = 0;

        for (WorkoutSession s : sessions) {
            totalDuration += s.getTotalDurationSeconds();
            totalCalories += s.getCaloriesBurned();
            List<WorkoutSet> sets = workoutRepository.findSetsBySessionId(s.getSessionId());
            for (WorkoutSet set : sets) {
                totalVolume += set.getWeight() * set.getReps();
            }
        }

        progress.setTotalDurationSeconds(totalDuration);
        progress.setTotalCaloriesBurned(totalCalories);
        progress.setTotalVolumeKg(totalVolume);
        progress.setAverageDurationSeconds(sessions.isEmpty() ? 0 : (double) totalDuration / sessions.size());
        return progress;
    }

    private WorkoutSession findSessionOrThrow(String sessionId) {
        return workoutRepository.findSessionById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutSession", sessionId));
    }

    private double estimateCalories(WorkoutSession session, List<WorkoutSet> sets) {
        // Simple estimation: base rate per minute + volume-based bonus
        double minutes = session.getTotalDurationSeconds() / 60.0;
        double volumeBonus = sets.stream().mapToDouble(s -> s.getWeight() * s.getReps() * 0.01).sum();
        return Math.round((minutes * 5.0 + volumeBonus) * 10.0) / 10.0;
    }
}
