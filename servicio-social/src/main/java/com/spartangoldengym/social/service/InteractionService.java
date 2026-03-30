package com.spartangoldengym.social.service;

import com.spartangoldengym.common.config.KafkaTopics;
import com.spartangoldengym.social.dto.CreateInteractionRequest;
import com.spartangoldengym.social.dto.InteractionResponse;
import com.spartangoldengym.social.entity.Interaction;
import com.spartangoldengym.social.repository.InteractionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InteractionService {

    private static final Logger log = LoggerFactory.getLogger(InteractionService.class);

    private final InteractionRepository interactionRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public InteractionService(InteractionRepository interactionRepository,
                              KafkaTemplate<String, String> kafkaTemplate) {
        this.interactionRepository = interactionRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public InteractionResponse createInteraction(CreateInteractionRequest request) {
        Interaction interaction = new Interaction();
        interaction.setUserId(request.getUserId());
        interaction.setType(request.getType());
        interaction.setTargetId(request.getTargetId());
        interaction.setTargetType(request.getTargetType());
        interaction.setContent(request.getContent());

        interaction = interactionRepository.save(interaction);

        // Publish to Kafka social.interactions
        String payload = String.format(
                "{\"interactionId\":\"%s\",\"userId\":\"%s\",\"type\":\"%s\","
                + "\"targetId\":\"%s\",\"targetType\":\"%s\",\"createdAt\":\"%s\"}",
                interaction.getId(), interaction.getUserId(), interaction.getType(),
                interaction.getTargetId(), interaction.getTargetType(), interaction.getCreatedAt());
        kafkaTemplate.send(KafkaTopics.SOCIAL_INTERACTIONS, request.getUserId().toString(), payload);

        log.info("Created interaction id={} type={} userId={} target={}/{}",
                interaction.getId(), interaction.getType(), interaction.getUserId(),
                interaction.getTargetType(), interaction.getTargetId());
        return toResponse(interaction);
    }

    InteractionResponse toResponse(Interaction interaction) {
        InteractionResponse r = new InteractionResponse();
        r.setId(interaction.getId());
        r.setUserId(interaction.getUserId());
        r.setType(interaction.getType());
        r.setTargetId(interaction.getTargetId());
        r.setTargetType(interaction.getTargetType());
        r.setContent(interaction.getContent());
        r.setCreatedAt(interaction.getCreatedAt());
        return r;
    }
}
