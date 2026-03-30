package com.spartangoldengym.social.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartangoldengym.social.dto.WorkoutSyncMessage;
import com.spartangoldengym.social.service.WorkoutSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.UUID;

/**
 * WebSocket handler for live workout synchronization.
 * Clients connect with query params: ?groupId=...&userId=...
 * Messages are broadcast to all other users in the same sync group.
 */
@Component
public class WorkoutSyncHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(WorkoutSyncHandler.class);

    private final WorkoutSyncService workoutSyncService;
    private final ObjectMapper objectMapper;

    public WorkoutSyncHandler(WorkoutSyncService workoutSyncService) {
        this.workoutSyncService = workoutSyncService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Map<String, String> params = parseQueryParams(session);
        String groupIdStr = params.get("groupId");
        String userIdStr = params.get("userId");

        if (groupIdStr == null || userIdStr == null) {
            log.warn("WebSocket connection missing groupId or userId, closing");
            try { session.close(CloseStatus.BAD_DATA); } catch (Exception ignored) {}
            return;
        }

        UUID groupId = UUID.fromString(groupIdStr);
        UUID userId = UUID.fromString(userIdStr);
        workoutSyncService.joinSyncGroup(groupId, userId, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        WorkoutSyncMessage syncMessage = objectMapper.readValue(
                message.getPayload(), WorkoutSyncMessage.class);
        workoutSyncService.broadcastProgress(session, syncMessage);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        workoutSyncService.leaveSyncGroup(session);
    }

    private Map<String, String> parseQueryParams(WebSocketSession session) {
        java.util.HashMap<String, String> params = new java.util.HashMap<>();
        if (session.getUri() != null && session.getUri().getQuery() != null) {
            for (String param : session.getUri().getQuery().split("&")) {
                String[] kv = param.split("=", 2);
                if (kv.length == 2) {
                    params.put(kv[0], kv[1]);
                }
            }
        }
        return params;
    }
}
