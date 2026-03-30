package com.spartangoldengym.social.entity;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "achievements")
public class Achievement {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "challenge_id")
    private UUID challengeId;

    @Column(nullable = false, length = 100)
    private String type; // "challenge_completed", "milestone", "streak"

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "badge_name", length = 255)
    private String badgeName;

    @Column(length = 500)
    private String description;

    @Column(name = "earned_at", nullable = false)
    private Instant earnedAt;

    @PrePersist
    protected void onCreate() {
        if (earnedAt == null) {
            earnedAt = Instant.now();
        }
    }

    public Achievement() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getChallengeId() { return challengeId; }
    public void setChallengeId(UUID challengeId) { this.challengeId = challengeId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBadgeName() { return badgeName; }
    public void setBadgeName(String badgeName) { this.badgeName = badgeName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getEarnedAt() { return earnedAt; }
    public void setEarnedAt(Instant earnedAt) { this.earnedAt = earnedAt; }
}
