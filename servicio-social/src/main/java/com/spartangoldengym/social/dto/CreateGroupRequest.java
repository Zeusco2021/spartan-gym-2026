package com.spartangoldengym.social.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class CreateGroupRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private UUID createdBy;

    private List<UUID> memberIds;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public List<UUID> getMemberIds() { return memberIds; }
    public void setMemberIds(List<UUID> memberIds) { this.memberIds = memberIds; }
}
