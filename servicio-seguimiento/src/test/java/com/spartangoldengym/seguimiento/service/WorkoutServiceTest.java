package com.spartangoldengym.seguimiento.service;

import com.spartangoldengym.common.exception.ResourceNotFoundException;
import com.spartangoldengym.seguimiento.dto.*;
import com.spartangoldengym.seguimiento.repository.InMemoryWorkoutRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private TimestreamMetricsService timestreamMetrics;

    private InMemoryWorkoutRepository repository;
    private WorkoutService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryWorkoutRepository();
        service = new WorkoutService(repository, kafkaTemplate, timestreamMetrics);
    }

    @Test
    void startWorkout_createsActiveSession() {
        StartWorkoutRequest request = new StartWorkoutRequest();
        request.setUserId("user-1");

        WorkoutSessionResponse response = service.startWorkout(request);

        assertNotNull(response.getSessionId());
        assertEquals("user-1", response.getUserId());
        assertEquals("active", response.getStatus());
        assertNotNull(response.getStartedAt());
        assertNull(response.getCompletedAt());
    }

    @Test
    void recordSet_addsSetToSession() {
        String sessionId = startSession("user-1");

        RecordSetRequest setReq = new RecordSetRequest();
        setReq.setExerciseId("exercise-bench");
        setReq.setWeight(80.0);
        setReq.setReps(10);
        setReq.setRestSeconds(90);

        WorkoutSetResponse response = service.recordSet(sessionId, setReq);

        assertNotNull(response.getSetId());
        assertEquals(sessionId, response.getSessionId());
        assertEquals("exercise-bench", response.getExerciseId());
        assertEquals(80.0, response.getWeight());
        assertEquals(10, response.getReps());
        assertEquals(90, response.getRestSeconds());
    }

    @Test
    void recordSet_nonExistentSession_throwsNotFound() {
        RecordSetRequest setReq = new RecordSetRequest();
        setReq.setExerciseId("ex-1");
        setReq.setWeight(50);
        setReq.setReps(8);

        assertThrows(ResourceNotFoundException.class,
                () -> service.recordSet("non-existent", setReq));
    }

    @Test
    void recordHeartRate_publishesToKafka() {
        String sessionId = startSession("user-1");

        RecordHeartRateRequest hrReq = new RecordHeartRateRequest();
        hrReq.setBpm(145);
        hrReq.setDeviceType("apple_watch");

        service.recordHeartRate(sessionId, hrReq);

        verify(kafkaTemplate).send(eq("real.time.heartrate"), eq("user-1"), contains("145"));
        verify(timestreamMetrics).recordHeartRate("user-1", sessionId, 145, "apple_watch");
    }

    @Test
    void recordHeartRate_nonExistentSession_throwsNotFound() {
        RecordHeartRateRequest hrReq = new RecordHeartRateRequest();
        hrReq.setBpm(120);

        assertThrows(ResourceNotFoundException.class,
                () -> service.recordHeartRate("non-existent", hrReq));
    }

    @Test
    void completeWorkout_setsCompletedStatusAndPublishes() {
        String sessionId = startSession("user-1");

        // Add a set first
        RecordSetRequest setReq = new RecordSetRequest();
        setReq.setExerciseId("ex-squat");
        setReq.setWeight(100);
        setReq.setReps(5);
        setReq.setRestSeconds(120);
        service.recordSet(sessionId, setReq);

        WorkoutSessionResponse response = service.completeWorkout(sessionId);

        assertEquals("completed", response.getStatus());
        assertNotNull(response.getCompletedAt());
        assertTrue(response.getTotalDurationSeconds() >= 0);
        assertTrue(response.getCaloriesBurned() >= 0);

        verify(kafkaTemplate).send(eq("workout.completed"), eq("user-1"), contains(sessionId));
        verify(timestreamMetrics).recordWorkoutMetrics(any(), anyList());
    }

    @Test
    void completeWorkout_nonExistentSession_throwsNotFound() {
        assertThrows(ResourceNotFoundException.class,
                () -> service.completeWorkout("non-existent"));
    }

    @Test
    void getWorkoutHistory_returnsAllSessionsForUser() {
        startSession("user-1");
        startSession("user-1");
        startSession("user-2");

        List<WorkoutSessionResponse> history = service.getWorkoutHistory("user-1");

        assertEquals(2, history.size());
        assertTrue(history.stream().allMatch(s -> "user-1".equals(s.getUserId())));
    }

    @Test
    void getWorkoutHistory_emptyForUnknownUser() {
        List<WorkoutSessionResponse> history = service.getWorkoutHistory("unknown");
        assertTrue(history.isEmpty());
    }

    @Test
    void getProgress_aggregatesCompletedSessions() {
        // Start and complete two sessions with sets
        String s1 = startSession("user-1");
        addSet(s1, "ex-1", 60, 10);
        service.completeWorkout(s1);

        String s2 = startSession("user-1");
        addSet(s2, "ex-2", 80, 8);
        service.completeWorkout(s2);

        // Start a third session but don't complete it
        startSession("user-1");

        WorkoutProgressResponse progress = service.getProgress("user-1");

        assertEquals("user-1", progress.getUserId());
        assertEquals(2, progress.getTotalWorkouts());
        assertTrue(progress.getTotalDurationSeconds() >= 0);
        assertTrue(progress.getTotalCaloriesBurned() > 0);
        // Volume = 60*10 + 80*8 = 600 + 640 = 1240
        assertEquals(1240.0, progress.getTotalVolumeKg(), 0.01);
        assertTrue(progress.getAverageDurationSeconds() >= 0);
    }

    @Test
    void getProgress_emptyForNewUser() {
        WorkoutProgressResponse progress = service.getProgress("new-user");

        assertEquals(0, progress.getTotalWorkouts());
        assertEquals(0, progress.getTotalDurationSeconds());
        assertEquals(0, progress.getTotalCaloriesBurned(), 0.01);
        assertEquals(0, progress.getTotalVolumeKg(), 0.01);
        assertEquals(0, progress.getAverageDurationSeconds(), 0.01);
    }

    @Test
    void recordSet_tracksUniqueExercisesInSession() {
        String sessionId = startSession("user-1");

        addSet(sessionId, "ex-bench", 80, 10);
        addSet(sessionId, "ex-bench", 85, 8);
        addSet(sessionId, "ex-squat", 100, 5);

        WorkoutSessionResponse session = service.getWorkoutHistory("user-1").get(0);
        assertEquals(2, session.getExercises().size());
        assertTrue(session.getExercises().contains("ex-bench"));
        assertTrue(session.getExercises().contains("ex-squat"));
    }

    // --- helpers ---

    private String startSession(String userId) {
        StartWorkoutRequest req = new StartWorkoutRequest();
        req.setUserId(userId);
        return service.startWorkout(req).getSessionId();
    }

    private void addSet(String sessionId, String exerciseId, double weight, int reps) {
        RecordSetRequest req = new RecordSetRequest();
        req.setExerciseId(exerciseId);
        req.setWeight(weight);
        req.setReps(reps);
        req.setRestSeconds(60);
        service.recordSet(sessionId, req);
    }
}
