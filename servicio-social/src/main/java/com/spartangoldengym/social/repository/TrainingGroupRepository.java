package com.spartangoldengym.social.repository;

import com.spartangoldengym.social.entity.TrainingGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TrainingGroupRepository extends JpaRepository<TrainingGroup, UUID> {

    @Query("SELECT g FROM TrainingGroup g JOIN g.memberIds m WHERE m = :userId")
    List<TrainingGroup> findByMemberId(@Param("userId") UUID userId);

    List<TrainingGroup> findByCreatedBy(UUID createdBy);
}
