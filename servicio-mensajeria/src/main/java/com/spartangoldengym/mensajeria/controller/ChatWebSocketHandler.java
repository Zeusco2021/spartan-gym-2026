package com.spartangoldengym.mensajeria.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.spartangoldengym.mensajeria.dto.MessageResponse;
import com.spartangoldengym.mensajeria.dto.SendMessageRequest;
import com.spartangoldengym.mensajeria.model.Conversation;
import com.spartangoldengym.mensajeria.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for real-time message delivery and read receipts.
 *
 * Validates: Requirements 25.3, 25.6, 25.7
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final MessageService messageService;
    private final ObjectMapper objectMapper;

    public ChatWebSocketHandler(MessageService messageService) {
        this.messageService = messageService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = getUserId(session);
        if (userId != null) {
            userSessions.put(userId, session);
            log.info("WebSocket connected: user={}", userId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = getUserId(session);
        if (userId != null) {
            userSessions.remove(userId);
            log.info("WebSocket disconnected: user={}", userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        String payload = textMessage.getPayload();
        JsonNode node = objectMapper.readTree(payload);
        String action = node.has("action") ? node.get("action").asText() : "";

        if ("send".equals(action)) {
            handleSendMessage(node);
        } else if ("read".equals(action)) {
            handleReadReceipt(node);
        } else {
            log.warn("Unknown WebSocket action: {}", action);
        }
    }

    private void handleSendMessage(JsonNode node) {
        try {
            SendMessageRequest request = new SendMessageRequest();
            request.setConversationId(node.get("conversationId").asText());
            request.setSenderId(node.get("senderId").asText());
            request.setContent(node.get("content").asText());
            request.setContentType(node.has("contentType") ? node.get("contentType").asText() : "text");
            request.setMediaDurationSeconds(node.has("mediaDurationSeconds")
                    ? node.get("mediaDurationSeconds").asInt() : 0);

            MessageResponse response = messageService.sendMessage(request);
            deliverToRecipients(request.getConversationId(), request.getSenderId(), response);
        } catch (Exception e) {
            log.error("Error handling send message via WebSocket", e);
        }
    }

    private void handleReadReceipt(JsonNode node) {
        try {
            String conversationId = node.get("conversationId").asText();
            String messageId = node.get("messageId").asText();
            String readerId = node.get("readerId").asText();

            MessageResponse response = messageService.markAsRead(conversationId, messageId, readerId);

            // Notify sender that message was read
            WebSocketSession senderSession = userSessions.get(response.getSenderId());
            if (senderSession != null && senderSession.isOpen()) {
                String readReceiptJson = objectMapper.writeValueAsString(
                        new ReadReceiptEvent(conversationId, messageId, readerId, response.getReadAt().toString()));
                senderSession.sendMessage(new TextMessage(readReceiptJson));
            }
        } catch (Exception e) {
            log.error("Error handling read receipt via WebSocket", e);
        }
    }

    private void deliverToRecipients(String conversationId, String senderId, MessageResponse response) {
        messageService.getConversations(senderId).stream()
                .filter(c -> conversationId.equals(c.getConversationId()))
                .findFirst()
                .ifPresent(conv -> {
                    for (String participantId : conv.getParticipantIds()) {
                        if (participantId.equals(senderId)) {
                            continue;
                        }
                        WebSocketSession recipientSession = userSessions.get(participantId);
                        if (recipientSession != null && recipientSession.isOpen()) {
                            try {
                                String json = objectMapper.writeValueAsString(response);
                                recipientSession.sendMessage(new TextMessage(json));
                            } catch (IOException e) {
                                log.error("Failed to deliver message to user {}", participantId, e);
                            }
                        } else {
                            messageService.notifyOfflineUser(participantId, toMessage(response));
                        }
                    }
                });
    }

    public boolean isUserOnline(String userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }

    private String getUserId(WebSocketSession session) {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        if (query != null && query.startsWith("userId=")) {
            return query.substring("userId=".length());
        }
        return null;
    }

    private com.spartangoldengym.mensajeria.model.Message toMessage(MessageResponse response) {
        com.spartangoldengym.mensajeria.model.Message msg = new com.spartangoldengym.mensajeria.model.Message();
        msg.setMessageId(response.getMessageId());
        msg.setConversationId(response.getConversationId());
        msg.setSenderId(response.getSenderId());
        return msg;
    }

    /**
     * Simple read receipt event POJO.
     */
    static class ReadReceiptEvent {
        private String conversationId;
        private String messageId;
        private String readerId;
        private String readAt;

        ReadReceiptEvent(String conversationId, String messageId, String readerId, String readAt) {
            this.conversationId = conversationId;
            this.messageId = messageId;
            this.readerId = readerId;
            this.readAt = readAt;
        }

        public String getConversationId() { return conversationId; }
        public String getMessageId() { return messageId; }
        public String getReaderId() { return readerId; }
        public String getReadAt() { return readAt; }
    }
}
