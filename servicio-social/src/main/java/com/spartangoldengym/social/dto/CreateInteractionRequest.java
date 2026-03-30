package com.spartangoldengym.social.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

public class CreateInteractionRequest {

    @NotNull
    private UUID userId;

    @NotBlank
    private String type; // "comment", "reaction", "share"

    private UUID targetId;

    private String targetType; // "achievement", "challenge", "workout"

    private String content;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public UUID getTargetId() { return targetId; }
    public void setTargetId(UUID targetId) { this.targetId = targetId; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
