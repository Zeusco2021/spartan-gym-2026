package com.spartangoldengym.entrenamiento.repository;

import com.spartangoldengym.entrenamiento.entity.RoutineExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoutineExerciseRepository extends JpaRepository<RoutineExercise, UUID> {

    List<RoutineExercise> findByRoutineIdOrderBySortOrder(UUID routineId);
}
