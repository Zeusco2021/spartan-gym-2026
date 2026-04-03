package com.spartangoldengym.reservas.repository;

import com.spartangoldengym.reservas.entity.ClassReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassReservationRepository extends JpaRepository<ClassReservation, UUID> {

    Optional<ClassReservation> findByClassIdAndUserIdAndStatus(UUID classId, UUID userId, String status);
}
