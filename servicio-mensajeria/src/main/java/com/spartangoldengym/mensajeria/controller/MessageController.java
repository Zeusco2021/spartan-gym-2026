package com.spartangoldengym.mensajeria.controller;

import com.spartangoldengym.mensajeria.dto.ConversationResponse;
import com.spartangoldengym.mensajeria.dto.MessageResponse;
import com.spartangoldengym.mensajeria.dto.SendMessageRequest;
import com.spartangoldengym.mensajeria.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for messaging endpoints.
 *
 * Validates: Requirements 25.1, 25.2, 25.3, 25.4, 25.5
 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponse>> getConversations(
            @RequestParam("userId") String userId) {
        List<ConversationResponse> conversations = messageService.getConversations(userId);
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/conversations/{id}")
    public ResponseEntity<List<MessageResponse>> getConversationHistory(
            @PathVariable("id") String conversationId) {
        List<MessageResponse> history = messageService.getConversationHistory(conversationId);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/send")
    public ResponseEntity<MessageResponse> sendMessage(@RequestBody SendMessageRequest request) {
        MessageResponse response = messageService.sendMessage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
