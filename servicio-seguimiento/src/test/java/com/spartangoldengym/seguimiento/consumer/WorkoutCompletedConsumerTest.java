package com.spartangoldengym.seguimiento.consumer;

import com.spartangoldengym.seguimiento.service.TrainerLookupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for WorkoutCompletedConsumer.
 * Validates: Requirement 10.3
 */
@ExtendWith(MockitoExtension.class)
class WorkoutCompletedConsumerTest {

    @Mock
    private TrainerLookupService trainerLookupService;

    private WorkoutCompletedConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new WorkoutCompletedConsumer(trainerLookupService);
    }

    @Test
    void processWorkoutCompleted_withTrainer_notifiesTrainer() {
        when(trainerLookupService.findTrainerForUser("user-1"))
                .thenReturn(Optional.of("trainer-42"));

        String event = "{\"sessionId\":\"sess-1\",\"userId\":\"user-1\",\"durationSeconds\":3600,"
                + "\"exercises\":5,\"sets\":15,\"caloriesBurned\":450.0,\"completedAt\":\"2024-01-15T10:00:00Z\"}";

        boolean result = consumer.processWorkoutCompleted(event);

        assertTrue(result);
        verify(trainerLookupService).findTrainerForUser("user-1");
    }

    @Test
    void processWorkoutCompleted_withoutTrainer_skipsNotification() {
        when(trainerLookupService.findTrainerForUser("user-2"))
                .thenReturn(Optional.empty());

        String event = "{\"sessionId\":\"sess-2\",\"userId\":\"user-2\",\"durationSeconds\":1800,"
                + "\"exercises\":3,\"sets\":9,\"caloriesBurned\":200.0,\"completedAt\":\"2024-01-15T11:00:00Z\"}";

        boolean result = consumer.processWorkoutCompleted(event);

        assertFalse(result);
        verify(trainerLookupService).findTrainerForUser("user-2");
    }

    @Test
    void processWorkoutCompleted_missingUserId_returnsFalse() {
        String event = "{\"sessionId\":\"sess-3\",\"durationSeconds\":900}";

        boolean result = consumer.processWorkoutCompleted(event);

        assertFalse(result);
        verifyNoInteractions(trainerLookupService);
    }

    @Test
    void processWorkoutCompleted_nullMessage_returnsFalse() {
        boolean result = consumer.processWorkoutCompleted(null);

        assertFalse(result);
        verifyNoInteractions(trainerLookupService);
    }

    @Test
    void extractField_extractsStringValue() {
        String json = "{\"userId\":\"user-1\",\"sessionId\":\"sess-1\"}";
        assertEquals("user-1", WorkoutCompletedConsumer.extractField(json, "userId"));
        assertEquals("sess-1", WorkoutCompletedConsumer.extractField(json, "sessionId"));
    }

    @Test
    void extractField_extractsNumericValue() {
        String json = "{\"durationSeconds\":3600,\"caloriesBurned\":450.5}";
        assertEquals("3600", WorkoutCompletedConsumer.extractField(json, "durationSeconds"));
        assertEquals("450.5", WorkoutCompletedConsumer.extractField(json, "caloriesBurned"));
    }

    @Test
    void extractField_returnsNullForMissingField() {
        String json = "{\"userId\":\"user-1\"}";
        assertNull(WorkoutCompletedConsumer.extractField(json, "trainerId"));
    }

    @Test
    void extractField_handlesNullInputs() {
        assertNull(WorkoutCompletedConsumer.extractField(null, "field"));
        assertNull(WorkoutCompletedConsumer.extractField("{}", null));
    }
}
