package com.spartangoldengym.seguimiento.repository;

import com.spartangoldengym.seguimiento.model.WorkoutSession;
import com.spartangoldengym.seguimiento.model.WorkoutSet;

import java.util.List;
import java.util.Optional;

/**
 * Abstraction for DynamoDB workout data operations.
 * Validates: Requirement 4.1, 12.2
 */
public interface WorkoutRepository {

    WorkoutSession saveSession(WorkoutSession session);

    Optional<WorkoutSession> findSessionById(String sessionId);

    List<WorkoutSession> findSessionsByUserId(String userId);

    WorkoutSet saveSet(WorkoutSet set);

    List<WorkoutSet> findSetsBySessionId(String sessionId);
}
