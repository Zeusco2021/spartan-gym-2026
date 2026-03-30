package com.spartangoldengym.social.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class GroupResponse {

    private UUID id;
    private String name;
    private String description;
    private UUID createdBy;
    private List<UUID> memberIds;
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public List<UUID> getMemberIds() { return memberIds; }
    public void setMemberIds(List<UUID> memberIds) { this.memberIds = memberIds; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
