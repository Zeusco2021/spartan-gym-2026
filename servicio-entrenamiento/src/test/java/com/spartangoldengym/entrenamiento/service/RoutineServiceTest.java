package com.spartangoldengym.entrenamiento.service;

import com.spartangoldengym.common.exception.ResourceNotFoundException;
import com.spartangoldengym.entrenamiento.dto.CreateRoutineRequest;
import com.spartangoldengym.entrenamiento.dto.RoutineResponse;
import com.spartangoldengym.entrenamiento.entity.Exercise;
import com.spartangoldengym.entrenamiento.entity.Routine;
import com.spartangoldengym.entrenamiento.entity.RoutineExercise;
import com.spartangoldengym.entrenamiento.entity.TrainingPlan;
import com.spartangoldengym.entrenamiento.repository.ExerciseRepository;
import com.spartangoldengym.entrenamiento.repository.RoutineExerciseRepository;
import com.spartangoldengym.entrenamiento.repository.RoutineRepository;
import com.spartangoldengym.entrenamiento.repository.TrainingPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoutineServiceTest {

    @Mock private RoutineRepository routineRepository;
    @Mock private RoutineExerciseRepository routineExerciseRepository;
    @Mock private TrainingPlanRepository planRepository;
    @Mock private ExerciseRepository exerciseRepository;

    private RoutineService service;

    @BeforeEach
    void setUp() {
        service = new RoutineService(routineRepository, routineExerciseRepository, planRepository, exerciseRepository);
    }

    @Test
    void createRoutine_withExercises_savesAll() {
        UUID planId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();

        TrainingPlan plan = new TrainingPlan();
        plan.setId(planId);

        Exercise exercise = new Exercise();
        exercise.setId(exerciseId);
        exercise.setName("Deadlift");

        CreateRoutineRequest.RoutineExerciseItem item = new CreateRoutineRequest.RoutineExerciseItem();
        item.setExerciseId(exerciseId);
        item.setSets(4);
        item.setReps("8-10");
        item.setRestSeconds(90);
        item.setSortOrder(1);

        CreateRoutineRequest request = new CreateRoutineRequest();
        request.setPlanId(planId);
        request.setName("Leg Day");
        request.setDayOfWeek(1);
        request.setSortOrder(1);
        request.setExercises(Collections.singletonList(item));

        Routine savedRoutine = new Routine();
        savedRoutine.setId(UUID.randomUUID());
        savedRoutine.setPlan(plan);
        savedRoutine.setName("Leg Day");
        savedRoutine.setDayOfWeek(1);
        savedRoutine.setSortOrder(1);

        RoutineExercise savedRe = new RoutineExercise();
        savedRe.setId(UUID.randomUUID());
        savedRe.setRoutine(savedRoutine);
        savedRe.setExercise(exercise);
        savedRe.setSets(4);
        savedRe.setReps("8-10");
        savedRe.setRestSeconds(90);
        savedRe.setSortOrder(1);

        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.of(exercise));
        when(routineRepository.save(any(Routine.class))).thenReturn(savedRoutine);
        when(routineExerciseRepository.save(any(RoutineExercise.class))).thenReturn(savedRe);

        RoutineResponse response = service.createRoutine(request);

        assertEquals("Leg Day", response.getName());
        assertEquals(1, response.getExercises().size());
        assertEquals("Deadlift", response.getExercises().get(0).getExerciseName());
        assertEquals(4, response.getExercises().get(0).getSets());
    }

    @Test
    void createRoutine_invalidPlan_throwsNotFound() {
        UUID planId = UUID.randomUUID();
        CreateRoutineRequest request = new CreateRoutineRequest();
        request.setPlanId(planId);
        request.setName("Test");

        when(planRepository.findById(planId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.createRoutine(request));
    }

    @Test
    void createRoutine_withoutExercises_savesRoutineOnly() {
        UUID planId = UUID.randomUUID();
        TrainingPlan plan = new TrainingPlan();
        plan.setId(planId);

        CreateRoutineRequest request = new CreateRoutineRequest();
        request.setPlanId(planId);
        request.setName("Rest Day");

        Routine savedRoutine = new Routine();
        savedRoutine.setId(UUID.randomUUID());
        savedRoutine.setPlan(plan);
        savedRoutine.setName("Rest Day");

        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(routineRepository.save(any(Routine.class))).thenReturn(savedRoutine);

        RoutineResponse response = service.createRoutine(request);

        assertEquals("Rest Day", response.getName());
        assertTrue(response.getExercises().isEmpty());
        verify(routineExerciseRepository, never()).save(any());
    }

    @Test
    void listRoutines_withPlanId_filtersResults() {
        UUID planId = UUID.randomUUID();
        TrainingPlan plan = new TrainingPlan();
        plan.setId(planId);

        Routine routine = new Routine();
        routine.setId(UUID.randomUUID());
        routine.setPlan(plan);
        routine.setName("Push Day");

        when(routineRepository.findByPlanIdOrderBySortOrder(planId)).thenReturn(Collections.singletonList(routine));
        when(routineExerciseRepository.findByRoutineIdOrderBySortOrder(routine.getId())).thenReturn(Collections.emptyList());

        List<RoutineResponse> results = service.listRoutines(planId);

        assertEquals(1, results.size());
        assertEquals("Push Day", results.get(0).getName());
    }

    @Test
    void listRoutines_withoutPlanId_returnsAll() {
        TrainingPlan plan = new TrainingPlan();
        plan.setId(UUID.randomUUID());

        Routine r1 = new Routine();
        r1.setId(UUID.randomUUID());
        r1.setPlan(plan);
        r1.setName("A");

        Routine r2 = new Routine();
        r2.setId(UUID.randomUUID());
        r2.setPlan(plan);
        r2.setName("B");

        when(routineRepository.findAll()).thenReturn(Arrays.asList(r1, r2));
        when(routineExerciseRepository.findByRoutineIdOrderBySortOrder(any())).thenReturn(Collections.emptyList());

        List<RoutineResponse> results = service.listRoutines(null);

        assertEquals(2, results.size());
    }
}
