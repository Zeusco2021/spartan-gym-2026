package com.spartangoldengym.mensajeria.repository;

import com.spartangoldengym.mensajeria.model.Conversation;
import com.spartangoldengym.mensajeria.model.Message;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory HashMap-based implementation of MessageRepository.
 * Serves as a stand-in for DynamoDB until the real client is available locally.
 *
 * Validates: Requirements 25.1, 25.5
 */
@Repository
public class InMemoryMessageRepository implements MessageRepository {

    private final Map<String, Message> messages = new ConcurrentHashMap<>();
    private final Map<String, Conversation> conversations = new ConcurrentHashMap<>();

    @Override
    public Message saveMessage(Message message) {
        String key = message.getConversationId() + "#" + message.getMessageId();
        messages.put(key, message);
        return message;
    }

    @Override
    public Optional<Message> findMessageById(String conversationId, String messageId) {
        String key = conversationId + "#" + messageId;
        return Optional.ofNullable(messages.get(key));
    }

    @Override
    public List<Message> findMessagesByConversationId(String conversationId) {
        return messages.values().stream()
                .filter(m -> conversationId.equals(m.getConversationId()))
                .sorted(Comparator.comparing(Message::getSentAt))
                .collect(Collectors.toList());
    }

    @Override
    public Conversation saveConversation(Conversation conversation) {
        conversations.put(conversation.getConversationId(), conversation);
        return conversation;
    }

    @Override
    public Optional<Conversation> findConversationById(String conversationId) {
        return Optional.ofNullable(conversations.get(conversationId));
    }

    @Override
    public List<Conversation> findConversationsByUserId(String userId) {
        return conversations.values().stream()
                .filter(c -> c.getParticipantIds().contains(userId))
                .sorted(Comparator.comparing(Conversation::getLastMessageAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }
}
