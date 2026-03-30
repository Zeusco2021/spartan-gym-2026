package com.spartangoldengym.gimnasio.repository;

import com.spartangoldengym.gimnasio.entity.GymChain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GymChainRepository extends JpaRepository<GymChain, UUID> {
}
