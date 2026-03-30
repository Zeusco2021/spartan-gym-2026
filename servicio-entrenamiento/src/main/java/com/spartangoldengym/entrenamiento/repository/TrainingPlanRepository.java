package com.spartangoldengym.entrenamiento.repository;

import com.spartangoldengym.entrenamiento.entity.TrainingPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TrainingPlanRepository extends JpaRepository<TrainingPlan, UUID> {

    List<TrainingPlan> findByUserId(UUID userId);

    List<TrainingPlan> findByTrainerId(UUID trainerId);
}
