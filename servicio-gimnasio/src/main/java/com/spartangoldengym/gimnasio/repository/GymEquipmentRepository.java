package com.spartangoldengym.gimnasio.repository;

import com.spartangoldengym.gimnasio.entity.GymEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GymEquipmentRepository extends JpaRepository<GymEquipment, UUID> {

    List<GymEquipment> findByGymId(UUID gymId);
}
