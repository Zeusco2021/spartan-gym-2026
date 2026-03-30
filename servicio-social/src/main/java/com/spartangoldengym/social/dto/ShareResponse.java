package com.spartangoldengym.social.dto;

import java.util.UUID;

public class ShareResponse {

    private UUID achievementId;
    private String imageUrl;
    private String shareText;
    private String platform;

    public UUID getAchievementId() { return achievementId; }
    public void setAchievementId(UUID achievementId) { this.achievementId = achievementId; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getShareText() { return shareText; }
    public void setShareText(String shareText) { this.shareText = shareText; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
}
