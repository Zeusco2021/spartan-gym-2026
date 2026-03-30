package com.spartangoldengym.social.service;

import com.spartangoldengym.common.exception.ResourceNotFoundException;
import com.spartangoldengym.social.dto.ShareRequest;
import com.spartangoldengym.social.dto.ShareResponse;
import com.spartangoldengym.social.entity.Achievement;
import com.spartangoldengym.social.repository.AchievementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ShareService {

    private static final Logger log = LoggerFactory.getLogger(ShareService.class);
    private static final String SHARE_IMAGE_BASE_URL = "https://cdn.spartangoldengym.com/share/";

    private final AchievementRepository achievementRepository;

    public ShareService(AchievementRepository achievementRepository) {
        this.achievementRepository = achievementRepository;
    }

    public ShareResponse generateShareCard(ShareRequest request) {
        Achievement achievement = achievementRepository.findById(request.getAchievementId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Achievement", "id=" + request.getAchievementId()));

        String platform = request.getPlatform() != null ? request.getPlatform() : "generic";

        // Generate shareable image URL (in production this would call an image generation service)
        String imageUrl = SHARE_IMAGE_BASE_URL + achievement.getId() + "/" + platform + ".png";

        String shareText = String.format(
                "\uD83C\uDFC6 I just earned the '%s' badge on Spartan Golden Gym! %s #SpartanGoldenGym #Fitness",
                achievement.getBadgeName(),
                achievement.getDescription() != null ? achievement.getDescription() : "");

        ShareResponse response = new ShareResponse();
        response.setAchievementId(achievement.getId());
        response.setImageUrl(imageUrl);
        response.setShareText(shareText);
        response.setPlatform(platform);

        log.info("Generated share card for achievement={} platform={}", achievement.getId(), platform);
        return response;
    }
}
