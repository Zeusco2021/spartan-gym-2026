package com.spartangoldengym.social.service;

import com.spartangoldengym.common.config.KafkaTopics;
import com.spartangoldengym.common.exception.ResourceNotFoundException;
import com.spartangoldengym.social.dto.AchievementResponse;
import com.spartangoldengym.social.dto.CompleteChallengeRequest;
import com.spartangoldengym.social.entity.Achievement;
import com.spartangoldengym.social.entity.Challenge;
import com.spartangoldengym.social.repository.AchievementRepository;
import com.spartangoldengym.social.repository.ChallengeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AchievementService {

    private static final Logger log = LoggerFactory.getLogger(AchievementService.class);

    private final AchievementRepository achievementRepository;
    private final ChallengeRepository challengeRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final RankingService rankingService;

    public AchievementService(AchievementRepository achievementRepository,
                              ChallengeRepository challengeRepository,
                              KafkaTemplate<String, String> kafkaTemplate,
                              RankingService rankingService) {
        this.achievementRepository = achievementRepository;
        this.challengeRepository = challengeRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.rankingService = rankingService;
    }

    @Transactional(readOnly = true)
    public List<AchievementResponse> getUserAchievements(UUID userId) {
        return achievementRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AchievementResponse completeChallenge(CompleteChallengeRequest request) {
        Challenge challenge = challengeRepository.findById(request.getChallengeId())
                .orElseThrow(() -> new ResourceNotFoundException("Challenge", "id=" + request.getChallengeId()));

        // Check if already completed
        List<Achievement> existing = achievementRepository
                .findByUserIdAndChallengeId(request.getUserId(), request.getChallengeId());
        if (!existing.isEmpty()) {
            return toResponse(existing.get(0));
        }

        // Create achievement / badge
        Achievement achievement = new Achievement();
        achievement.setUserId(request.getUserId());
        achievement.setChallengeId(challenge.getId());
        achievement.setType("challenge_completed");
        achievement.setName("Completed: " + challenge.getName());
        achievement.setBadgeName(challenge.getBadgeName() != null
                ? challenge.getBadgeName()
                : challenge.getName() + " Badge");
        achievement.setDescription("Achieved " + request.getAchievedValue()
                + " / " + challenge.getTargetValue() + " " + challenge.getMetricName());

        achievement = achievementRepository.save(achievement);

        // Update ranking for the challenge category
        double score = request.getAchievedValue() != null ? request.getAchievedValue() : challenge.getTargetValue();
        rankingService.updateScore(challenge.getCategory(), request.getUserId(), score);

        // Publish to Kafka user.achievements
        String payload = String.format(
                "{\"achievementId\":\"%s\",\"userId\":\"%s\",\"challengeId\":\"%s\","
                + "\"type\":\"challenge_completed\",\"name\":\"%s\",\"badgeName\":\"%s\","
                + "\"earnedAt\":\"%s\"}",
                achievement.getId(), achievement.getUserId(), challenge.getId(),
                achievement.getName(), achievement.getBadgeName(), achievement.getEarnedAt());
        kafkaTemplate.send(KafkaTopics.USER_ACHIEVEMENTS, request.getUserId().toString(), payload);

        log.info("User {} completed challenge {} — badge '{}' awarded, published to Kafka",
                request.getUserId(), challenge.getId(), achievement.getBadgeName());

        return toResponse(achievement);
    }

    AchievementResponse toResponse(Achievement a) {
        AchievementResponse r = new AchievementResponse();
        r.setId(a.getId());
        r.setUserId(a.getUserId());
        r.setChallengeId(a.getChallengeId());
        r.setType(a.getType());
        r.setName(a.getName());
        r.setBadgeName(a.getBadgeName());
        r.setDescription(a.getDescription());
        r.setEarnedAt(a.getEarnedAt());
        return r;
    }
}
