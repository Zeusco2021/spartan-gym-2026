package com.spartangoldengym.social.service;

import com.spartangoldengym.social.dto.WorkoutSyncMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WorkoutSyncServiceTest {

    private WorkoutSyncService syncService;

    @BeforeEach
    void setUp() {
        syncService = new WorkoutSyncService();
    }

    @Test
    void joinSyncGroup_addsSessionToGroup() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        WebSocketSession session = mockSession("s1");

        syncService.joinSyncGroup(groupId, userId, session);

        assertEquals(1, syncService.getActiveSessionCount(groupId));
    }

    @Test
    void leaveSyncGroup_removesSessionFromGroup() {
        UUID groupId = UUID.randomUUID();
        WebSocketSession session = mockSession("s1");

        syncService.joinSyncGroup(groupId, UUID.randomUUID(), session);
        syncService.leaveSyncGroup(session);

        assertEquals(0, syncService.getActiveSessionCount(groupId));
    }

    @Test
    void broadcastProgress_withTwoUsers_sendsToOther() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        WebSocketSession session1 = mockSession("s1");
        WebSocketSession session2 = mockSession("s2");

        syncService.joinSyncGroup(groupId, user1, session1);
        syncService.joinSyncGroup(groupId, user2, session2);

        WorkoutSyncMessage message = new WorkoutSyncMessage(
                user1, UUID.randomUUID(), "set_completed", "bench press 100kg x 10");

        syncService.broadcastProgress(session1, message);

        // session2 should receive the message, session1 should not
        verify(session2).sendMessage(any(TextMessage.class));
        verify(session1, never()).sendMessage(any(TextMessage.class));
    }

    @Test
    void broadcastProgress_singleUser_doesNotBroadcast() throws Exception {
        UUID groupId = UUID.randomUUID();
        WebSocketSession session = mockSession("s1");

        syncService.joinSyncGroup(groupId, UUID.randomUUID(), session);

        WorkoutSyncMessage message = new WorkoutSyncMessage(
                UUID.randomUUID(), UUID.randomUUID(), "set_completed", "data");

        syncService.broadcastProgress(session, message);

        verify(session, never()).sendMessage(any(TextMessage.class));
    }

    @Test
    void broadcastProgress_noGroup_doesNothing() throws Exception {
        WebSocketSession session = mockSession("s1");

        WorkoutSyncMessage message = new WorkoutSyncMessage(
                UUID.randomUUID(), UUID.randomUUID(), "set_completed", "data");

        // Should not throw
        syncService.broadcastProgress(session, message);
    }

    @Test
    void broadcastProgress_threeUsers_sendsToOtherTwo() throws Exception {
        UUID groupId = UUID.randomUUID();
        WebSocketSession s1 = mockSession("s1");
        WebSocketSession s2 = mockSession("s2");
        WebSocketSession s3 = mockSession("s3");

        syncService.joinSyncGroup(groupId, UUID.randomUUID(), s1);
        syncService.joinSyncGroup(groupId, UUID.randomUUID(), s2);
        syncService.joinSyncGroup(groupId, UUID.randomUUID(), s3);

        WorkoutSyncMessage message = new WorkoutSyncMessage(
                UUID.randomUUID(), UUID.randomUUID(), "heartrate_update", "145");

        syncService.broadcastProgress(s1, message);

        verify(s1, never()).sendMessage(any(TextMessage.class));
        verify(s2).sendMessage(any(TextMessage.class));
        verify(s3).sendMessage(any(TextMessage.class));
    }

    @Test
    void broadcastProgress_closedSession_skipsGracefully() throws Exception {
        UUID groupId = UUID.randomUUID();
        WebSocketSession s1 = mockSession("s1");
        WebSocketSession s2 = mock(WebSocketSession.class);
        when(s2.getId()).thenReturn("s2");
        when(s2.isOpen()).thenReturn(false);

        syncService.joinSyncGroup(groupId, UUID.randomUUID(), s1);
        syncService.joinSyncGroup(groupId, UUID.randomUUID(), s2);

        WorkoutSyncMessage message = new WorkoutSyncMessage(
                UUID.randomUUID(), UUID.randomUUID(), "set_completed", "data");

        syncService.broadcastProgress(s1, message);

        verify(s2, never()).sendMessage(any(TextMessage.class));
    }

    private WebSocketSession mockSession(String id) {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn(id);
        when(session.isOpen()).thenReturn(true);
        return session;
    }
}
