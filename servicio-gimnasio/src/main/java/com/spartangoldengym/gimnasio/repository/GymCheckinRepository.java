package com.spartangoldengym.gimnasio.repository;

import com.spartangoldengym.gimnasio.entity.GymCheckin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GymCheckinRepository extends JpaRepository<GymCheckin, UUID> {

    long countByGymIdAndCheckedOutAtIsNull(UUID gymId);
}
