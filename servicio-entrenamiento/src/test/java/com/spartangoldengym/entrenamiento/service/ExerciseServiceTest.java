package com.spartangoldengym.entrenamiento.service;

import com.spartangoldengym.common.exception.ResourceNotFoundException;
import com.spartangoldengym.entrenamiento.dto.ExerciseResponse;
import com.spartangoldengym.entrenamiento.entity.Exercise;
import com.spartangoldengym.entrenamiento.repository.ExerciseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExerciseServiceTest {

    @Mock
    private ExerciseRepository exerciseRepository;

    private ExerciseService service;

    @BeforeEach
    void setUp() {
        service = new ExerciseService(exerciseRepository);
    }

    @Test
    void listExercises_returnsPagedResults() {
        Exercise exercise = buildExercise("Bench Press");
        Page<Exercise> page = new PageImpl<>(Collections.singletonList(exercise));
        when(exerciseRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<ExerciseResponse> result = service.listExercises(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("Bench Press", result.getContent().get(0).getName());
    }

    @Test
    void getExercise_existingId_returnsResponse() {
        UUID exerciseId = UUID.randomUUID();
        Exercise exercise = buildExercise("Squat");
        exercise.setId(exerciseId);
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.of(exercise));

        ExerciseResponse response = service.getExercise(exerciseId);

        assertEquals(exerciseId, response.getId());
        assertEquals("Squat", response.getName());
        assertEquals("[\"chest\",\"triceps\"]", response.getMuscleGroups());
    }

    @Test
    void getExercise_nonExistingId_throwsNotFound() {
        UUID exerciseId = UUID.randomUUID();
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getExercise(exerciseId));
    }

    private Exercise buildExercise(String name) {
        Exercise exercise = new Exercise();
        exercise.setId(UUID.randomUUID());
        exercise.setName(name);
        exercise.setMuscleGroups("[\"chest\",\"triceps\"]");
        exercise.setEquipmentRequired("[\"barbell\",\"bench\"]");
        exercise.setDifficulty("intermediate");
        exercise.setVideoUrl("https://example.com/video.mp4");
        exercise.setInstructions("Lie on bench, press barbell up");
        return exercise;
    }
}
