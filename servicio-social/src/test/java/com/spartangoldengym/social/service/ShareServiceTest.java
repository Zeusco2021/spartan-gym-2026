package com.spartangoldengym.social.service;

import com.spartangoldengym.common.exception.ResourceNotFoundException;
import com.spartangoldengym.social.dto.ShareRequest;
import com.spartangoldengym.social.dto.ShareResponse;
import com.spartangoldengym.social.entity.Achievement;
import com.spartangoldengym.social.repository.AchievementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShareServiceTest {

    @Mock
    private AchievementRepository achievementRepository;

    @InjectMocks
    private ShareService shareService;

    private UUID userId;
    private UUID achievementId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        achievementId = UUID.randomUUID();
    }

    @Test
    void generateShareCard_returnsCardWithImageAndText() {
        Achievement achievement = makeAchievement();
        when(achievementRepository.findById(achievementId)).thenReturn(Optional.of(achievement));

        ShareRequest request = new ShareRequest();
        request.setUserId(userId);
        request.setAchievementId(achievementId);
        request.setPlatform("instagram");

        ShareResponse response = shareService.generateShareCard(request);

        assertEquals(achievementId, response.getAchievementId());
        assertEquals("instagram", response.getPlatform());
        assertTrue(response.getImageUrl().contains(achievementId.toString()));
        assertTrue(response.getImageUrl().contains("instagram"));
        assertTrue(response.getShareText().contains("Iron Chest"));
        assertTrue(response.getShareText().contains("#SpartanGoldenGym"));
    }

    @Test
    void generateShareCard_noPlatform_usesGeneric() {
        Achievement achievement = makeAchievement();
        when(achievementRepository.findById(achievementId)).thenReturn(Optional.of(achievement));

        ShareRequest request = new ShareRequest();
        request.setUserId(userId);
        request.setAchievementId(achievementId);

        ShareResponse response = shareService.generateShareCard(request);

        assertEquals("generic", response.getPlatform());
        assertTrue(response.getImageUrl().contains("generic"));
    }

    @Test
    void generateShareCard_achievementNotFound_throwsException() {
        when(achievementRepository.findById(achievementId)).thenReturn(Optional.empty());

        ShareRequest request = new ShareRequest();
        request.setUserId(userId);
        request.setAchievementId(achievementId);

        assertThrows(ResourceNotFoundException.class,
                () -> shareService.generateShareCard(request));
    }

    // --- Helpers ---

    private Achievement makeAchievement() {
        Achievement a = new Achievement();
        a.setId(achievementId);
        a.setUserId(userId);
        a.setType("challenge_completed");
        a.setName("Completed: Bench Press Challenge");
        a.setBadgeName("Iron Chest");
        a.setDescription("Achieved 550.0 / 500.0 weight_lifted_kg");
        a.setEarnedAt(Instant.now());
        return a;
    }
}
