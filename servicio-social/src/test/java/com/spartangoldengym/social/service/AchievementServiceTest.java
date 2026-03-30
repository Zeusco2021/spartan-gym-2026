package com.spartangoldengym.social.service;

import com.spartangoldengym.common.exception.ResourceNotFoundException;
import com.spartangoldengym.social.dto.AchievementResponse;
import com.spartangoldengym.social.dto.CompleteChallengeRequest;
import com.spartangoldengym.social.entity.Achievement;
import com.spartangoldengym.social.entity.Challenge;
import com.spartangoldengym.social.repository.AchievementRepository;
import com.spartangoldengym.social.repository.ChallengeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AchievementServiceTest {

    @Mock
    private AchievementRepository achievementRepository;
    @Mock
    private ChallengeRepository challengeRepository;
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;
    @Mock
    private RankingService rankingService;

    private AchievementService achievementService;

    private UUID userId;
    private UUID challengeId;

    @BeforeEach
    void setUp() {
        achievementService = new AchievementService(
                achievementRepository, challengeRepository, kafkaTemplate, rankingService);
        userId = UUID.randomUUID();
        challengeId = UUID.randomUUID();
    }

    @Test
    void getUserAchievements_returnsUserAchievements() {
        Achievement a = makeAchievement();
        when(achievementRepository.findByUserId(userId)).thenReturn(Collections.singletonList(a));

        List<AchievementResponse> result = achievementService.getUserAchievements(userId);

        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getUserId());
        assertEquals("challenge_completed", result.get(0).getType());
    }

    @Test
    void getUserAchievements_noAchievements_returnsEmpty() {
        when(achievementRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        List<AchievementResponse> result = achievementService.getUserAchievements(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void completeChallenge_createsAchievementAndPublishesToKafka() {
        Challenge challenge = makeChallenge();
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(achievementRepository.findByUserIdAndChallengeId(userId, challengeId))
                .thenReturn(Collections.emptyList());
        when(achievementRepository.save(any(Achievement.class))).thenAnswer(inv -> {
            Achievement a = inv.getArgument(0);
            a.setId(UUID.randomUUID());
            if (a.getEarnedAt() == null) a.setEarnedAt(Instant.now());
            return a;
        });

        CompleteChallengeRequest request = new CompleteChallengeRequest();
        request.setUserId(userId);
        request.setChallengeId(challengeId);
        request.setAchievedValue(550.0);

        AchievementResponse response = achievementService.completeChallenge(request);

        assertNotNull(response.getId());
        assertEquals("challenge_completed", response.getType());
        assertNotNull(response.getBadgeName());

        // Verify Kafka publish
        verify(kafkaTemplate).send(eq("user.achievements"), eq(userId.toString()), any(String.class));

        // Verify ranking update
        verify(rankingService).updateScore("strength", userId, 550.0);
    }

    @Test
    void completeChallenge_alreadyCompleted_returnsExisting() {
        Challenge challenge = makeChallenge();
        Achievement existing = makeAchievement();
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(achievementRepository.findByUserIdAndChallengeId(userId, challengeId))
                .thenReturn(Collections.singletonList(existing));

        CompleteChallengeRequest request = new CompleteChallengeRequest();
        request.setUserId(userId);
        request.setChallengeId(challengeId);
        request.setAchievedValue(550.0);

        AchievementResponse response = achievementService.completeChallenge(request);

        assertEquals(existing.getId(), response.getId());
        // Should NOT publish to Kafka or save again
        verify(achievementRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    void completeChallenge_challengeNotFound_throwsException() {
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.empty());

        CompleteChallengeRequest request = new CompleteChallengeRequest();
        request.setUserId(userId);
        request.setChallengeId(challengeId);

        assertThrows(ResourceNotFoundException.class,
                () -> achievementService.completeChallenge(request));
    }

    // --- Helpers ---

    private Challenge makeChallenge() {
        Challenge c = new Challenge();
        c.setId(challengeId);
        c.setName("Bench Press Challenge");
        c.setType("weekly");
        c.setCategory("strength");
        c.setMetricName("weight_lifted_kg");
        c.setTargetValue(500.0);
        c.setBadgeName("Iron Chest");
        c.setCreatedBy(UUID.randomUUID());
        c.setStartsAt(Instant.now());
        c.setEndsAt(Instant.now().plus(7, ChronoUnit.DAYS));
        c.setCreatedAt(Instant.now());
        return c;
    }

    private Achievement makeAchievement() {
        Achievement a = new Achievement();
        a.setId(UUID.randomUUID());
        a.setUserId(userId);
        a.setChallengeId(challengeId);
        a.setType("challenge_completed");
        a.setName("Completed: Bench Press Challenge");
        a.setBadgeName("Iron Chest");
        a.setDescription("Achieved 550.0 / 500.0 weight_lifted_kg");
        a.setEarnedAt(Instant.now());
        return a;
    }
}
