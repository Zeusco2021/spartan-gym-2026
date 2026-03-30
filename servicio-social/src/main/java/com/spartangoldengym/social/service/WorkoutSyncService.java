package com.spartangoldengym.social.service;

import com.spartangoldengym.social.dto.WorkoutSyncMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages live workout synchronization between users training simultaneously.
 * When 2+ users are in the same sync group, progress updates are broadcast in real time.
 */
@Service
public class WorkoutSyncService {

    private static final Logger log = LoggerFactory.getLogger(WorkoutSyncService.class);

    // groupId -> set of active sessions
    private final Map<UUID, Set<WebSocketSession>> syncGroups = new ConcurrentHashMap<>();
    // sessionId -> groupId for reverse lookup
    private final Map<String, UUID> sessionToGroup = new ConcurrentHashMap<>();
    // sessionId -> userId
    private final Map<String, UUID> sessionToUser = new ConcurrentHashMap<>();

    public void joinSyncGroup(UUID groupId, UUID userId, WebSocketSession session) {
        syncGroups.computeIfAbsent(groupId, k -> ConcurrentHashMap.newKeySet()).add(session);
        sessionToGroup.put(session.getId(), groupId);
        sessionToUser.put(session.getId(), userId);
        log.info("User {} joined workout sync group {} (active sessions: {})",
                userId, groupId, syncGroups.get(groupId).size());
    }

    public void leaveSyncGroup(WebSocketSession session) {
        UUID groupId = sessionToGroup.remove(session.getId());
        UUID userId = sessionToUser.remove(session.getId());
        if (groupId != null) {
            Set<WebSocketSession> sessions = syncGroups.get(groupId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    syncGroups.remove(groupId);
                }
            }
            log.info("User {} left workout sync group {}", userId, groupId);
        }
    }

    public void broadcastProgress(WebSocketSession sender, WorkoutSyncMessage message) {
        UUID groupId = sessionToGroup.get(sender.getId());
        if (groupId == null) {
            return;
        }

        Set<WebSocketSession> sessions = syncGroups.get(groupId);
        if (sessions == null || sessions.size() < 2) {
            return; // Need 2+ users for live sync
        }

        String json = String.format(
                "{\"userId\":\"%s\",\"sessionId\":\"%s\",\"eventType\":\"%s\",\"payload\":%s}",
                message.getUserId(), message.getSessionId(),
                message.getEventType(),
                message.getPayload() != null ? "\"" + message.getPayload() + "\"" : "null");

        TextMessage textMessage = new TextMessage(json);
        for (WebSocketSession session : sessions) {
            if (session.isOpen() && !session.getId().equals(sender.getId())) {
                try {
                    session.sendMessage(textMessage);
                } catch (IOException e) {
                    log.warn("Failed to send sync message to session {}: {}",
                            session.getId(), e.getMessage());
                }
            }
        }
    }

    // Visible for testing
    int getActiveSessionCount(UUID groupId) {
        Set<WebSocketSession> sessions = syncGroups.get(groupId);
        return sessions != null ? sessions.size() : 0;
    }
}
