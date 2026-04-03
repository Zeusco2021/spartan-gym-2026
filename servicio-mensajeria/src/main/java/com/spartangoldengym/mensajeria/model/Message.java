package com.spartangoldengym.mensajeria.model;

import java.time.Instant;

/**
 * Represents a message stored in DynamoDB.
 * PK: conversationId, SK: messageId
 *
 * Validates: Requirements 25.1, 25.4, 25.5
 */
public class Message {

    private String conversationId;
    private String messageId;
    private String senderId;
    private String content;
    private String contentType; // "text", "image", "video", "voice"
    private String status; // "sent", "delivered", "read"
    private Instant sentAt;
    private Instant readAt;
    private int mediaDurationSeconds; // for video/voice

    public Message() {
    }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }

    public Instant getReadAt() { return readAt; }
    public void setReadAt(Instant readAt) { this.readAt = readAt; }

    public int getMediaDurationSeconds() { return mediaDurationSeconds; }
    public void setMediaDurationSeconds(int mediaDurationSeconds) { this.mediaDurationSeconds = mediaDurationSeconds; }
}
