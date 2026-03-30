package com.spartangoldengym.gimnasio.repository;

import com.spartangoldengym.gimnasio.entity.Gym;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GymRepository extends JpaRepository<Gym, UUID> {

    List<Gym> findByChainId(UUID chainId);
}
