package com.spartangoldengym.entrenamiento.service;

import com.spartangoldengym.common.exception.ResourceNotFoundException;
import com.spartangoldengym.entrenamiento.dto.ExerciseResponse;
import com.spartangoldengym.entrenamiento.entity.Exercise;
import com.spartangoldengym.entrenamiento.repository.ExerciseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;

    public ExerciseService(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    @Transactional(readOnly = true)
    public Page<ExerciseResponse> listExercises(Pageable pageable) {
        return exerciseRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ExerciseResponse getExercise(UUID id) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", id.toString()));
        return toResponse(exercise);
    }

    ExerciseResponse toResponse(Exercise exercise) {
        ExerciseResponse response = new ExerciseResponse();
        response.setId(exercise.getId());
        response.setName(exercise.getName());
        response.setMuscleGroups(exercise.getMuscleGroups());
        response.setEquipmentRequired(exercise.getEquipmentRequired());
        response.setDifficulty(exercise.getDifficulty());
        response.setVideoUrl(exercise.getVideoUrl());
        response.setInstructions(exercise.getInstructions());
        return response;
    }
}
