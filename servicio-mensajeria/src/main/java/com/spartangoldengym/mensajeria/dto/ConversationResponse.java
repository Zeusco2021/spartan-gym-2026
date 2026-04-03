package com.spartangoldengym.mensajeria.dto;

import java.time.Instant;
import java.util.List;

/**
 * DTO for conversation response.
 *
 * Validates: Requirements 25.1, 25.2, 25.5
 */
public class ConversationResponse {

    private String conversationId;
    private List<String> participantIds;
    private String type;
    private Instant lastMessageAt;
    private int unreadCount;

    public ConversationResponse() {
    }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public List<String> getParticipantIds() { return participantIds; }
    public void setParticipantIds(List<String> participantIds) { this.participantIds = participantIds; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Instant getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(Instant lastMessageAt) { this.lastMessageAt = lastMessageAt; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
}
