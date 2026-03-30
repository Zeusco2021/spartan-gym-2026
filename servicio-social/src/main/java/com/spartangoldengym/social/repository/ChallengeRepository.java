package com.spartangoldengym.social.repository;

import com.spartangoldengym.social.entity.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, UUID> {

    List<Challenge> findByType(String type);

    List<Challenge> findByCategory(String category);

    List<Challenge> findByTypeAndCategory(String type, String category);
}
