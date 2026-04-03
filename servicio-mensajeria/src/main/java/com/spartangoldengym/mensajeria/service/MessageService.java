package com.spartangoldengym.mensajeria.service;

import com.spartangoldengym.mensajeria.dto.ConversationResponse;
import com.spartangoldengym.mensajeria.dto.MessageResponse;
import com.spartangoldengym.mensajeria.dto.SendMessageRequest;
import com.spartangoldengym.mensajeria.model.Conversation;
import com.spartangoldengym.mensajeria.model.Message;
import com.spartangoldengym.mensajeria.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Business logic for messaging: send, list conversations, get history, mark as read.
 *
 * Validates: Requirements 25.1, 25.2, 25.3, 25.4, 25.5, 25.6, 25.7, 25.8
 */
@Service
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    private static final int MAX_GROUP_PARTICIPANTS = 100;
    private static final int MAX_VIDEO_DURATION_SECONDS = 60;
    private static final int MAX_VOICE_DURATION_SECONDS = 120;
    private static final Set<String> ALLOWED_CONTENT_TYPES =
            new HashSet<>(Arrays.asList("text", "image", "video", "voice"));

    private final MessageRepository messageRepository;
    private final MessageEncryptionService encryptionService;

    public MessageService(MessageRepository messageRepository,
                          MessageEncryptionService encryptionService) {
        this.messageRepository = messageRepository;
        this.encryptionService = encryptionService;
    }

    public MessageResponse sendMessage(SendMessageRequest request) {
        validateContentType(request.getContentType());
        validateMediaDuration(request.getContentType(), request.getMediaDurationSeconds());

        Conversation conversation = messageRepository.findConversationById(request.getConversationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Conversation not found: " + request.getConversationId()));

        if (!conversation.getParticipantIds().contains(request.getSenderId())) {
            throw new IllegalArgumentException("Sender is not a participant of this conversation");
        }

        Message message = new Message();
        message.setConversationId(request.getConversationId());
        message.setMessageId(UUID.randomUUID().toString());
        message.setSenderId(request.getSenderId());
        message.setContent(encryptionService.encrypt(request.getContent()));
        message.setContentType(request.getContentType());
        message.setMediaDurationSeconds(request.getMediaDurationSeconds());
        message.setStatus("sent");
        message.setSentAt(Instant.now());

        messageRepository.saveMessage(message);

        conversation.setLastMessageAt(message.getSentAt());
        messageRepository.saveConversation(conversation);

        return toMessageResponse(message);
    }

    public List<ConversationResponse> getConversations(String userId) {
        return messageRepository.findConversationsByUserId(userId).stream()
                .map(this::toConversationResponse)
                .collect(Collectors.toList());
    }

    public List<MessageResponse> getConversationHistory(String conversationId) {
        messageRepository.findConversationById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Conversation not found: " + conversationId));

        return messageRepository.findMessagesByConversationId(conversationId).stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList());
    }

    public MessageResponse markAsRead(String conversationId, String messageId, String readerId) {
        Message message = messageRepository.findMessageById(conversationId, messageId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Message not found: " + messageId));

        if (message.getSenderId().equals(readerId)) {
            throw new IllegalArgumentException("Sender cannot mark own message as read");
        }

        message.setStatus("read");
        message.setReadAt(Instant.now());
        messageRepository.saveMessage(message);

        return toMessageResponse(message);
    }

    public Conversation createConversation(String type, List<String> participantIds) {
        if ("group".equals(type) && participantIds.size() > MAX_GROUP_PARTICIPANTS) {
            throw new IllegalArgumentException(
                    "Group chat cannot exceed " + MAX_GROUP_PARTICIPANTS + " participants");
        }
        if ("direct".equals(type) && participantIds.size() != 2) {
            throw new IllegalArgumentException("Direct conversation must have exactly 2 participants");
        }

        Conversation conversation = new Conversation();
        conversation.setConversationId(UUID.randomUUID().toString());
        conversation.setType(type);
        conversation.setParticipantIds(participantIds);
        conversation.setLastMessageAt(Instant.now());
        conversation.setUnreadCount(0);

        return messageRepository.saveConversation(conversation);
    }

    public boolean isUserOnline(String userId) {
        // Placeholder: in production, check WebSocket session registry
        return false;
    }

    public void notifyOfflineUser(String userId, Message message) {
        log.info("User {} is offline. Push notification should be sent via Servicio_Notificaciones " +
                "for message {} in conversation {}", userId, message.getMessageId(), message.getConversationId());
    }

    private void validateContentType(String contentType) {
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Invalid content type: " + contentType
                    + ". Allowed: " + ALLOWED_CONTENT_TYPES);
        }
    }

    private void validateMediaDuration(String contentType, int durationSeconds) {
        if ("video".equals(contentType) && durationSeconds > MAX_VIDEO_DURATION_SECONDS) {
            throw new IllegalArgumentException(
                    "Video duration exceeds maximum of " + MAX_VIDEO_DURATION_SECONDS + " seconds");
        }
        if ("voice".equals(contentType) && durationSeconds > MAX_VOICE_DURATION_SECONDS) {
            throw new IllegalArgumentException(
                    "Voice message duration exceeds maximum of " + MAX_VOICE_DURATION_SECONDS + " seconds");
        }
    }

    private MessageResponse toMessageResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setMessageId(message.getMessageId());
        response.setConversationId(message.getConversationId());
        response.setSenderId(message.getSenderId());
        response.setContent(encryptionService.decrypt(message.getContent()));
        response.setContentType(message.getContentType());
        response.setStatus(message.getStatus());
        response.setSentAt(message.getSentAt());
        response.setReadAt(message.getReadAt());
        response.setMediaDurationSeconds(message.getMediaDurationSeconds());
        return response;
    }

    private ConversationResponse toConversationResponse(Conversation conversation) {
        ConversationResponse response = new ConversationResponse();
        response.setConversationId(conversation.getConversationId());
        response.setParticipantIds(conversation.getParticipantIds());
        response.setType(conversation.getType());
        response.setLastMessageAt(conversation.getLastMessageAt());
        response.setUnreadCount(conversation.getUnreadCount());
        return response;
    }
}
