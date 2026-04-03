package com.spartangoldengym.mensajeria.dto;

/**
 * DTO for sending a message.
 *
 * Validates: Requirements 25.1, 25.4
 */
public class SendMessageRequest {

    private String conversationId;
    private String senderId;
    private String content;
    private String contentType; // "text", "image", "video", "voice"
    private int mediaDurationSeconds; // for video/voice

    public SendMessageRequest() {
    }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public int getMediaDurationSeconds() { return mediaDurationSeconds; }
    public void setMediaDurationSeconds(int mediaDurationSeconds) { this.mediaDurationSeconds = mediaDurationSeconds; }
}
