package com.spartangoldengym.reservas.repository;

import com.spartangoldengym.reservas.entity.TrainerAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TrainerAvailabilityRepository extends JpaRepository<TrainerAvailability, UUID> {

    List<TrainerAvailability> findByTrainerId(UUID trainerId);

    void deleteByTrainerId(UUID trainerId);
}
