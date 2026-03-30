package com.spartangoldengym.social.repository;

import com.spartangoldengym.social.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, UUID> {

    List<Achievement> findByUserId(UUID userId);

    List<Achievement> findByUserIdAndChallengeId(UUID userId, UUID challengeId);
}
