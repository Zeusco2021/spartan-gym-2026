package com.spartangoldengym.mensajeria.service;

import com.spartangoldengym.mensajeria.dto.ConversationResponse;
import com.spartangoldengym.mensajeria.dto.MessageResponse;
import com.spartangoldengym.mensajeria.dto.SendMessageRequest;
import com.spartangoldengym.mensajeria.model.Conversation;
import com.spartangoldengym.mensajeria.repository.InMemoryMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MessageService.
 *
 * Validates: Requirements 25.1, 25.2, 25.4, 25.5, 25.6, 25.8
 */
class MessageServiceTest {

    private MessageService messageService;
    private InMemoryMessageRepository repository;
    private MessageEncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        repository = new InMemoryMessageRepository();
        encryptionService = new MessageEncryptionService();
        messageService = new MessageService(repository, encryptionService);
    }

    @Test
    void sendMessage_textMessage_succeeds() {
        Conversation conv = messageService.createConversation("direct", Arrays.asList("user1", "user2"));

        SendMessageRequest request = new SendMessageRequest();
        request.setConversationId(conv.getConversationId());
        request.setSenderId("user1");
        request.setContent("Hello!");
        request.setContentType("text");

        MessageResponse response = messageService.sendMessage(request);

        assertNotNull(response.getMessageId());
        assertEquals("Hello!", response.getContent());
        assertEquals("text", response.getContentType());
        assertEquals("sent", response.getStatus());
        assertNotNull(response.getSentAt());
    }

    @Test
    void sendMessage_invalidContentType_throws() {
        Conversation conv = messageService.createConversation("direct", Arrays.asList("user1", "user2"));

        SendMessageRequest request = new SendMessageRequest();
        request.setConversationId(conv.getConversationId());
        request.setSenderId("user1");
        request.setContent("data");
        request.setContentType("pdf");

        assertThrows(IllegalArgumentException.class, () -> messageService.sendMessage(request));
    }

    @Test
    void sendMessage_videoExceedsDuration_throws() {
        Conversation conv = messageService.createConversation("direct", Arrays.asList("user1", "user2"));

        SendMessageRequest request = new SendMessageRequest();
        request.setConversationId(conv.getConversationId());
        request.setSenderId("user1");
        request.setContent("video-url");
        request.setContentType("video");
        request.setMediaDurationSeconds(61);

        assertThrows(IllegalArgumentException.class, () -> messageService.sendMessage(request));
    }

    @Test
    void sendMessage_voiceExceedsDuration_throws() {
        Conversation conv = messageService.createConversation("direct", Arrays.asList("user1", "user2"));

        SendMessageRequest request = new SendMessageRequest();
        request.setConversationId(conv.getConversationId());
        request.setSenderId("user1");
        request.setContent("voice-url");
        request.setContentType("voice");
        request.setMediaDurationSeconds(121);

        assertThrows(IllegalArgumentException.class, () -> messageService.sendMessage(request));
    }

    @Test
    void sendMessage_nonParticipant_throws() {
        Conversation conv = messageService.createConversation("direct", Arrays.asList("user1", "user2"));

        SendMessageRequest request = new SendMessageRequest();
        request.setConversationId(conv.getConversationId());
        request.setSenderId("user3");
        request.setContent("Hello!");
        request.setContentType("text");

        assertThrows(IllegalArgumentException.class, () -> messageService.sendMessage(request));
    }

    @Test
    void getConversations_returnsUserConversations() {
        messageService.createConversation("direct", Arrays.asList("user1", "user2"));
        messageService.createConversation("direct", Arrays.asList("user1", "user3"));
        messageService.createConversation("direct", Arrays.asList("user2", "user3"));

        List<ConversationResponse> result = messageService.getConversations("user1");

        assertEquals(2, result.size());
    }

    @Test
    void getConversationHistory_returnsMessages() {
        Conversation conv = messageService.createConversation("direct", Arrays.asList("user1", "user2"));

        SendMessageRequest req1 = new SendMessageRequest();
        req1.setConversationId(conv.getConversationId());
        req1.setSenderId("user1");
        req1.setContent("First");
        req1.setContentType("text");
        messageService.sendMessage(req1);

        SendMessageRequest req2 = new SendMessageRequest();
        req2.setConversationId(conv.getConversationId());
        req2.setSenderId("user2");
        req2.setContent("Second");
        req2.setContentType("text");
        messageService.sendMessage(req2);

        List<MessageResponse> history = messageService.getConversationHistory(conv.getConversationId());

        assertEquals(2, history.size());
        assertEquals("First", history.get(0).getContent());
        assertEquals("Second", history.get(1).getContent());
    }

    @Test
    void markAsRead_updatesStatus() {
        Conversation conv = messageService.createConversation("direct", Arrays.asList("user1", "user2"));

        SendMessageRequest request = new SendMessageRequest();
        request.setConversationId(conv.getConversationId());
        request.setSenderId("user1");
        request.setContent("Read me");
        request.setContentType("text");
        MessageResponse sent = messageService.sendMessage(request);

        MessageResponse read = messageService.markAsRead(
                conv.getConversationId(), sent.getMessageId(), "user2");

        assertEquals("read", read.getStatus());
        assertNotNull(read.getReadAt());
    }

    @Test
    void markAsRead_senderCannotMarkOwn_throws() {
        Conversation conv = messageService.createConversation("direct", Arrays.asList("user1", "user2"));

        SendMessageRequest request = new SendMessageRequest();
        request.setConversationId(conv.getConversationId());
        request.setSenderId("user1");
        request.setContent("My message");
        request.setContentType("text");
        MessageResponse sent = messageService.sendMessage(request);

        assertThrows(IllegalArgumentException.class, () ->
                messageService.markAsRead(conv.getConversationId(), sent.getMessageId(), "user1"));
    }

    @Test
    void createConversation_groupExceedsLimit_throws() {
        String[] participants = new String[101];
        for (int i = 0; i < 101; i++) {
            participants[i] = "user" + i;
        }

        assertThrows(IllegalArgumentException.class, () ->
                messageService.createConversation("group", Arrays.asList(participants)));
    }

    @Test
    void createConversation_directMustHaveTwoParticipants_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                messageService.createConversation("direct", Arrays.asList("user1", "user2", "user3")));
    }

    @Test
    void createConversation_groupUpTo100_succeeds() {
        String[] participants = new String[100];
        for (int i = 0; i < 100; i++) {
            participants[i] = "user" + i;
        }

        Conversation conv = messageService.createConversation("group", Arrays.asList(participants));

        assertNotNull(conv.getConversationId());
        assertEquals("group", conv.getType());
        assertEquals(100, conv.getParticipantIds().size());
    }
}
