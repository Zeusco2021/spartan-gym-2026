package com.spartangoldengym.social.service;

import com.spartangoldengym.social.dto.CreateInteractionRequest;
import com.spartangoldengym.social.dto.InteractionResponse;
import com.spartangoldengym.social.entity.Interaction;
import com.spartangoldengym.social.repository.InteractionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InteractionServiceTest {

    @Mock
    private InteractionRepository interactionRepository;
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private InteractionService interactionService;

    @BeforeEach
    void setUp() {
        interactionService = new InteractionService(interactionRepository, kafkaTemplate);
    }

    @Test
    void createInteraction_comment_savesAndPublishesToKafka() {
        UUID userId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        CreateInteractionRequest request = new CreateInteractionRequest();
        request.setUserId(userId);
        request.setType("comment");
        request.setTargetId(targetId);
        request.setTargetType("achievement");
        request.setContent("Great job!");

        when(interactionRepository.save(any(Interaction.class))).thenAnswer(inv -> {
            Interaction i = inv.getArgument(0);
            i.setId(UUID.randomUUID());
            i.setCreatedAt(Instant.now());
            return i;
        });

        InteractionResponse response = interactionService.createInteraction(request);

        assertNotNull(response.getId());
        assertEquals(userId, response.getUserId());
        assertEquals("comment", response.getType());
        assertEquals(targetId, response.getTargetId());
        assertEquals("achievement", response.getTargetType());
        assertEquals("Great job!", response.getContent());
        assertNotNull(response.getCreatedAt());

        verify(kafkaTemplate).send(eq("social.interactions"), eq(userId.toString()), any(String.class));
    }

    @Test
    void createInteraction_reaction_savesAndPublishesToKafka() {
        UUID userId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        CreateInteractionRequest request = new CreateInteractionRequest();
        request.setUserId(userId);
        request.setType("reaction");
        request.setTargetId(targetId);
        request.setTargetType("workout");
        request.setContent(null);

        when(interactionRepository.save(any(Interaction.class))).thenAnswer(inv -> {
            Interaction i = inv.getArgument(0);
            i.setId(UUID.randomUUID());
            i.setCreatedAt(Instant.now());
            return i;
        });

        InteractionResponse response = interactionService.createInteraction(request);

        assertNotNull(response.getId());
        assertEquals("reaction", response.getType());
        assertNull(response.getContent());

        verify(kafkaTemplate).send(eq("social.interactions"), eq(userId.toString()), any(String.class));
    }

    @Test
    void createInteraction_share_savesAndPublishesToKafka() {
        UUID userId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        CreateInteractionRequest request = new CreateInteractionRequest();
        request.setUserId(userId);
        request.setType("share");
        request.setTargetId(targetId);
        request.setTargetType("challenge");

        when(interactionRepository.save(any(Interaction.class))).thenAnswer(inv -> {
            Interaction i = inv.getArgument(0);
            i.setId(UUID.randomUUID());
            i.setCreatedAt(Instant.now());
            return i;
        });

        InteractionResponse response = interactionService.createInteraction(request);

        assertEquals("share", response.getType());
        verify(kafkaTemplate).send(eq("social.interactions"), eq(userId.toString()), any(String.class));
    }
}
