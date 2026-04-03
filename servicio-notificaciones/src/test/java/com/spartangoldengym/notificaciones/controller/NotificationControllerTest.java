package com.spartangoldengym.notificaciones.controller;

import com.spartangoldengym.notificaciones.dto.*;
import com.spartangoldengym.notificaciones.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    private NotificationController controller;

    @BeforeEach
    void setUp() {
        controller = new NotificationController(notificationService);
    }

    // --- GET /preferences ---

    @Test
    void getPreferences_returnsUserPreferences() {
        NotificationPreferenceResponse pref = new NotificationPreferenceResponse();
        pref.setUserId("user-1");
        pref.setQuietHoursEnabled(true);
        pref.setQuietHoursStart("22:00");
        pref.setQuietHoursEnd("07:00");

        when(notificationService.getPreferences("user-1")).thenReturn(pref);

        ResponseEntity<NotificationPreferenceResponse> response =
                controller.getPreferences("user-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("user-1", response.getBody().getUserId());
        assertTrue(response.getBody().isQuietHoursEnabled());
        verify(notificationService).getPreferences("user-1");
    }

    // --- PUT /preferences ---

    @Test
    void updatePreferences_setsUserIdFromHeaderAndReturnsUpdated() {
        NotificationPreferenceRequest request = new NotificationPreferenceRequest();
        Map<String, Map<String, Boolean>> channels = new HashMap<>();
        Map<String, Boolean> training = new HashMap<>();
        training.put("push", true);
        training.put("email", false);
        training.put("sms", false);
        channels.put("entrenamientos", training);
        request.setCategoryChannels(channels);
        request.setQuietHoursEnabled(true);
        request.setQuietHoursStart("23:00");
        request.setQuietHoursEnd("08:00");

        NotificationPreferenceResponse expected = new NotificationPreferenceResponse();
        expected.setUserId("user-2");
        expected.setCategoryChannels(channels);
        expected.setQuietHoursEnabled(true);

        when(notificationService.updatePreferences(any(NotificationPreferenceRequest.class)))
                .thenReturn(expected);

        ResponseEntity<NotificationPreferenceResponse> response =
                controller.updatePreferences("user-2", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("user-2", response.getBody().getUserId());
        // Verify userId was set from header
        assertEquals("user-2", request.getUserId());
        verify(notificationService).updatePreferences(request);
    }

    // --- GET /history ---

    @Test
    void getHistory_returnsAllNotificationsForUser() {
        List<NotificationHistoryResponse> history = Arrays.asList(
                createHistory("n1", "push", "sent", "entrenamientos"),
                createHistory("n2", "email", "read", "social"),
                createHistory("n3", "sms", "failed", "pagos")
        );
        when(notificationService.getHistory("user-1")).thenReturn(history);

        ResponseEntity<List<NotificationHistoryResponse>> response =
                controller.getHistory("user-1", null, null, 50);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().size());
    }

    @Test
    void getHistory_filtersByChannel() {
        List<NotificationHistoryResponse> history = Arrays.asList(
                createHistory("n1", "push", "sent", "entrenamientos"),
                createHistory("n2", "email", "read", "social"),
                createHistory("n3", "push", "failed", "pagos")
        );
        when(notificationService.getHistory("user-1")).thenReturn(history);

        ResponseEntity<List<NotificationHistoryResponse>> response =
                controller.getHistory("user-1", "push", null, 50);

        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().stream().allMatch(n -> "push".equals(n.getChannel())));
    }

    @Test
    void getHistory_filtersByStatus() {
        List<NotificationHistoryResponse> history = Arrays.asList(
                createHistory("n1", "push", "sent", "entrenamientos"),
                createHistory("n2", "email", "sent", "social"),
                createHistory("n3", "sms", "failed", "pagos")
        );
        when(notificationService.getHistory("user-1")).thenReturn(history);

        ResponseEntity<List<NotificationHistoryResponse>> response =
                controller.getHistory("user-1", null, "sent", 50);

        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().stream().allMatch(n -> "sent".equals(n.getStatus())));
    }

    @Test
    void getHistory_filtersByChannelAndStatus() {
        List<NotificationHistoryResponse> history = Arrays.asList(
                createHistory("n1", "push", "sent", "entrenamientos"),
                createHistory("n2", "push", "failed", "social"),
                createHistory("n3", "email", "sent", "pagos")
        );
        when(notificationService.getHistory("user-1")).thenReturn(history);

        ResponseEntity<List<NotificationHistoryResponse>> response =
                controller.getHistory("user-1", "push", "sent", 50);

        assertEquals(1, response.getBody().size());
        assertEquals("n1", response.getBody().get(0).getNotificationId());
    }

    @Test
    void getHistory_respectsLimit() {
        List<NotificationHistoryResponse> history = Arrays.asList(
                createHistory("n1", "push", "sent", "entrenamientos"),
                createHistory("n2", "email", "sent", "social"),
                createHistory("n3", "sms", "sent", "pagos")
        );
        when(notificationService.getHistory("user-1")).thenReturn(history);

        ResponseEntity<List<NotificationHistoryResponse>> response =
                controller.getHistory("user-1", null, null, 2);

        assertEquals(2, response.getBody().size());
    }

    // --- POST /schedule ---

    @Test
    void scheduleNotification_returns201WithScheduledNotification() {
        ScheduleNotificationRequest request = new ScheduleNotificationRequest();
        request.setTitle("Recordatorio de entrenamiento");
        request.setContent("Tu sesión comienza en 30 minutos");
        request.setCategory("entrenamientos");
        request.setScheduledAt(Instant.now().plusSeconds(1800));

        NotificationHistoryResponse expected = new NotificationHistoryResponse();
        expected.setNotificationId("notif-123");
        expected.setStatus("scheduled");
        expected.setTitle("Recordatorio de entrenamiento");

        when(notificationService.scheduleNotification(any(ScheduleNotificationRequest.class)))
                .thenReturn(expected);

        ResponseEntity<NotificationHistoryResponse> response =
                controller.scheduleNotification("user-1", request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("scheduled", response.getBody().getStatus());
        assertEquals("notif-123", response.getBody().getNotificationId());
        // Verify userId was set from header
        assertEquals("user-1", request.getUserId());
        verify(notificationService).scheduleNotification(request);
    }

    // --- helpers ---

    private NotificationHistoryResponse createHistory(String id, String channel,
                                                       String status, String category) {
        NotificationHistoryResponse response = new NotificationHistoryResponse();
        response.setNotificationId(id);
        response.setChannel(channel);
        response.setStatus(status);
        response.setCategory(category);
        response.setTitle("Test notification " + id);
        response.setContent("Content for " + id);
        response.setSentAt(Instant.now());
        return response;
    }
}
