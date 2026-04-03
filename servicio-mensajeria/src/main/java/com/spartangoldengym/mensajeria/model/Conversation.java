package com.spartangoldengym.mensajeria.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a conversation stored in DynamoDB.
 * PK: userId, SK: conversationId
 *
 * Validates: Requirements 25.1, 25.2, 25.5
 */
public class Conversation {

    private String conversationId;
    private String userId;
    private List<String> participantIds;
    private String type; // "direct", "group"
    private Instant lastMessageAt;
    private int unreadCount;

    public Conversation() {
        this.participantIds = new ArrayList<>();
    }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<String> getParticipantIds() { return participantIds; }
    public void setParticipantIds(List<String> participantIds) { this.participantIds = participantIds; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Instant getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(Instant lastMessageAt) { this.lastMessageAt = lastMessageAt; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
}
