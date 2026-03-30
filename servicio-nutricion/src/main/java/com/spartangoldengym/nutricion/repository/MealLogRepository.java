package com.spartangoldengym.nutricion.repository;

import com.spartangoldengym.nutricion.entity.MealLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface MealLogRepository extends JpaRepository<MealLog, UUID> {

    List<MealLog> findByUserIdAndLoggedAtBetween(UUID userId, Instant start, Instant end);
}
