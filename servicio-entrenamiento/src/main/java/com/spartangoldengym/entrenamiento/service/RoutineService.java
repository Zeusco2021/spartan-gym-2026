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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final RoutineExerciseRepository routineExerciseRepository;
    private final TrainingPlanRepository planRepository;
    private final ExerciseRepository exerciseRepository;

    public RoutineService(RoutineRepository routineRepository,
                          RoutineExerciseRepository routineExerciseRepository,
                          TrainingPlanRepository planRepository,
                          ExerciseRepository exerciseRepository) {
        this.routineRepository = routineRepository;
        this.routineExerciseRepository = routineExerciseRepository;
        this.planRepository = planRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @Transactional
    public RoutineResponse createRoutine(CreateRoutineRequest request) {
        TrainingPlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("TrainingPlan", request.getPlanId().toString()));

        Routine routine = new Routine();
        routine.setPlan(plan);
        routine.setName(request.getName());
        routine.setDayOfWeek(request.getDayOfWeek());
        routine.setSortOrder(request.getSortOrder());
        routine = routineRepository.save(routine);

        List<RoutineExercise> savedExercises = new ArrayList<>();
        if (request.getExercises() != null) {
            for (CreateRoutineRequest.RoutineExerciseItem item : request.getExercises()) {
                Exercise exercise = exerciseRepository.findById(item.getExerciseId())
                        .orElseThrow(() -> new ResourceNotFoundException("Exercise", item.getExerciseId().toString()));

                RoutineExercise re = new RoutineExercise();
                re.setRoutine(routine);
                re.setExercise(exercise);
                re.setSets(item.getSets());
                re.setReps(item.getReps());
                re.setRestSeconds(item.getRestSeconds());
                re.setSortOrder(item.getSortOrder());
                savedExercises.add(routineExerciseRepository.save(re));
            }
        }

        return toResponse(routine, savedExercises);
    }

    @Transactional(readOnly = true)
    public List<RoutineResponse> listRoutines(UUID planId) {
        List<Routine> routines = (planId != null)
                ? routineRepository.findByPlanIdOrderBySortOrder(planId)
                : routineRepository.findAll();

        return routines.stream().map(routine -> {
            List<RoutineExercise> exercises = routineExerciseRepository.findByRoutineIdOrderBySortOrder(routine.getId());
            return toResponse(routine, exercises);
        }).collect(Collectors.toList());
    }

    RoutineResponse toResponse(Routine routine, List<RoutineExercise> exercises) {
        RoutineResponse response = new RoutineResponse();
        response.setId(routine.getId());
        response.setPlanId(routine.getPlan() != null ? routine.getPlan().getId() : null);
        response.setName(routine.getName());
        response.setDayOfWeek(routine.getDayOfWeek());
        response.setSortOrder(routine.getSortOrder());

        List<RoutineResponse.RoutineExerciseResponse> exerciseResponses = exercises.stream().map(re -> {
            RoutineResponse.RoutineExerciseResponse er = new RoutineResponse.RoutineExerciseResponse();
            er.setId(re.getId());
            er.setExerciseId(re.getExercise() != null ? re.getExercise().getId() : null);
            er.setExerciseName(re.getExercise() != null ? re.getExercise().getName() : null);
            er.setSets(re.getSets());
            er.setReps(re.getReps());
            er.setRestSeconds(re.getRestSeconds());
            er.setSortOrder(re.getSortOrder());
            return er;
        }).collect(Collectors.toList());

        response.setExercises(exerciseResponses);
        return response;
    }
}
