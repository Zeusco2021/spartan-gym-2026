package com.spartangoldengym.mensajeria.repository;

import com.spartangoldengym.mensajeria.model.Conversation;
import com.spartangoldengym.mensajeria.model.Message;

import java.util.List;
import java.util.Optional;

/**
 * Abstraction for DynamoDB messaging data operations.
 *
 * Validates: Requirements 25.1, 25.5
 */
public interface MessageRepository {

    Message saveMessage(Message message);

    Optional<Message> findMessageById(String conversationId, String messageId);

    List<Message> findMessagesByConversationId(String conversationId);

    Conversation saveConversation(Conversation conversation);

    Optional<Conversation> findConversationById(String conversationId);

    List<Conversation> findConversationsByUserId(String userId);
}
