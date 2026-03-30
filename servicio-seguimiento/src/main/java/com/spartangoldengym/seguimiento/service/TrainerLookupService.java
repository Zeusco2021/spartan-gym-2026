package com.spartangoldengym.seguimiento.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Stub service for looking up the assigned trainer for a given user.
 * In production, this would call Servicio_Entrenamiento or query a local cache.
 *
 * Validates: Requirement 10.3
 */
@Service
public class TrainerLookupService {

    private static final Logger log = LoggerFactory.getLogger(TrainerLookupService.class);

    /**
     * Returns the trainer ID assigned to the given user, if any.
     * Stub implementation: returns a trainer for demonstration purposes.
     */
    public Optional<String> findTrainerForUser(String userId) {
        log.debug("Looking up trainer for userId={}", userId);
        // Stub: in production, call Servicio_Entrenamiento or query training_plans table
        return Optional.empty();
    }
}
