package com.spartangoldengym.social.repository;

import com.spartangoldengym.social.entity.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, UUID> {

    List<Interaction> findByUserId(UUID userId);

    List<Interaction> findByTargetIdAndTargetType(UUID targetId, String targetType);
}
