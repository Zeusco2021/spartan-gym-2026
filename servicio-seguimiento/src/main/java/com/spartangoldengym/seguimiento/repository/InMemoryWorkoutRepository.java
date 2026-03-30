package com.spartangoldengym.seguimiento.repository;

import com.spartangoldengym.seguimiento.model.WorkoutSession;
import com.spartangoldengym.seguimiento.model.WorkoutSet;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory HashMap-based implementation of WorkoutRepository.
 * Serves as a stand-in for DynamoDB until the real client is available locally.
 */
@Repository
public class InMemoryWorkoutRepository implements WorkoutRepository {

    private final Map<String, WorkoutSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, List<WorkoutSet>> setsBySession = new ConcurrentHashMap<>();

    @Override
    public WorkoutSession saveSession(WorkoutSession session) {
        sessions.put(session.getSessionId(), session);
        return session;
    }

    @Override
    public Optional<WorkoutSession> findSessionById(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    @Override
    public List<WorkoutSession> findSessionsByUserId(String userId) {
        return sessions.values().stream()
                .filter(s -> userId.equals(s.getUserId()))
                .collect(Collectors.toList());
    }

    @Override
    public WorkoutSet saveSet(WorkoutSet set) {
        setsBySession.computeIfAbsent(set.getSessionId(), k -> Collections.synchronizedList(new ArrayList<>()))
                .add(set);
        return set;
    }

    @Override
    public List<WorkoutSet> findSetsBySessionId(String sessionId) {
        return setsBySession.getOrDefault(sessionId, Collections.emptyList());
    }
}
