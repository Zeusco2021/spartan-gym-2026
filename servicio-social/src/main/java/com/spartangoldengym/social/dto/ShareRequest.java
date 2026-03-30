package com.spartangoldengym.social.dto;

import javax.validation.constraints.NotNull;
import java.util.UUID;

public class ShareRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private UUID achievementId;

    private String platform; // "instagram", "twitter", "facebook"

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getAchievementId() { return achievementId; }
    public void setAchievementId(UUID achievementId) { this.achievementId = achievementId; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
}
