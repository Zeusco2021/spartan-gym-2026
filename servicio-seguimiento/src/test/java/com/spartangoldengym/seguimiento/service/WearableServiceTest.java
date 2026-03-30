package com.spartangoldengym.seguimiento.service;

import com.spartangoldengym.seguimiento.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WearableServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private TimestreamMetricsService timestreamMetrics;

    private WearableService service;

    @BeforeEach
    void setUp() {
        service = new WearableService(timestreamMetrics, kafkaTemplate);
    }

    // --- connect tests ---

    @Test
    void connectWearable_appleWatch_returnsConnected() {
        WearableConnectRequest req = connectRequest("user-1", "apple_watch", "AW-123");

        WearableConnectResponse resp = service.connectWearable(req);

        assertNotNull(resp.getConnectionId());
        assertEquals("user-1", resp.getUserId());
        assertEquals("apple_watch", resp.getDeviceType());
        assertEquals("AW-123", resp.getDeviceId());
        assertEquals("connected", resp.getStatus());
        assertNotNull(resp.getConnectedAt());
    }

    @Test
    void connectWearable_fitbit_returnsConnected() {
        WearableConnectResponse resp = service.connectWearable(
                connectRequest("user-2", "fitbit", "FB-456"));
        assertEquals("connected", resp.getStatus());
        assertEquals("fitbit", resp.getDeviceType());
    }

    @Test
    void connectWearable_garmin_returnsConnected() {
        WearableConnectResponse resp = service.connectWearable(
                connectRequest("user-3", "garmin", "GM-789"));
        assertEquals("connected", resp.getStatus());
        assertEquals("garmin", resp.getDeviceType());
    }

    @Test
    void connectWearable_unsupportedDevice_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> service.connectWearable(connectRequest("user-1", "xiaomi_band", "XB-1")));
    }

    @Test
    void connectWearable_nullUserId_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> service.connectWearable(connectRequest(null, "fitbit", "FB-1")));
    }

    @Test
    void connectWearable_storesConnection() {
        WearableConnectResponse resp = service.connectWearable(
                connectRequest("user-1", "apple_watch", "AW-1"));
        assertTrue(service.getConnections().containsKey(resp.getConnectionId()));
    }

    // --- sync tests ---

    @Test
    void syncData_storesBiometricDataInTimestream() {
        WearableSyncRequest req = syncRequest("user-1", "fitbit",
                entry("heart_rate", 72, Instant.now()),
                entry("steps", 8500, Instant.now()));

        WearableSyncResponse resp = service.syncData(req);

        assertEquals("user-1", resp.getUserId());
        assertEquals(2, resp.getRecordsSynced());
        assertNotNull(resp.getSyncedAt());
        verify(timestreamMetrics).recordBiometricDataBatch(argThat(list -> list.size() == 2));
    }

    @Test
    void syncData_publishesHeartRateToKafka() {
        WearableSyncRequest req = syncRequest("user-1", "apple_watch",
                entry("heart_rate", 145, Instant.now()),
                entry("steps", 3000, Instant.now()));

        service.syncData(req);

        verify(kafkaTemplate, times(1)).send(eq("real.time.heartrate"), eq("user-1"), contains("145"));
    }

    @Test
    void syncData_doesNotPublishNonHeartRateToKafka() {
        WearableSyncRequest req = syncRequest("user-1", "garmin",
                entry("steps", 10000, Instant.now()),
                entry("calories", 2200, Instant.now()),
                entry("sleep", 7.5, Instant.now()));

        service.syncData(req);

        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void syncData_filtersInvalidDataTypes() {
        WearableSyncRequest req = syncRequest("user-1", "fitbit",
                entry("heart_rate", 80, Instant.now()),
                entry("invalid_type", 999, Instant.now()));

        WearableSyncResponse resp = service.syncData(req);

        assertEquals(1, resp.getRecordsSynced());
    }

    @Test
    void syncData_nullUserId_throwsIllegalArgument() {
        WearableSyncRequest req = syncRequest(null, "fitbit",
                entry("steps", 5000, Instant.now()));

        assertThrows(IllegalArgumentException.class, () -> service.syncData(req));
    }

    @Test
    void syncData_emptyData_throwsIllegalArgument() {
        WearableSyncRequest req = new WearableSyncRequest();
        req.setUserId("user-1");
        req.setDeviceType("fitbit");
        req.setData(Collections.emptyList());

        assertThrows(IllegalArgumentException.class, () -> service.syncData(req));
    }

    @Test
    void syncData_nullTimestamp_defaultsToNow() {
        WearableSyncRequest req = syncRequest("user-1", "garmin",
                entry("steps", 4000, null));

        WearableSyncResponse resp = service.syncData(req);

        assertEquals(1, resp.getRecordsSynced());
        verify(timestreamMetrics).recordBiometricDataBatch(argThat(list ->
                list.get(0).getTimestamp() != null));
    }

    // --- helpers ---

    private WearableConnectRequest connectRequest(String userId, String deviceType, String deviceId) {
        WearableConnectRequest req = new WearableConnectRequest();
        req.setUserId(userId);
        req.setDeviceType(deviceType);
        req.setDeviceId(deviceId);
        return req;
    }

    private WearableSyncRequest syncRequest(String userId, String deviceType,
                                            WearableSyncRequest.BiometricEntry... entries) {
        WearableSyncRequest req = new WearableSyncRequest();
        req.setUserId(userId);
        req.setDeviceType(deviceType);
        req.setData(Arrays.asList(entries));
        return req;
    }

    private WearableSyncRequest.BiometricEntry entry(String dataType, double value, Instant timestamp) {
        WearableSyncRequest.BiometricEntry e = new WearableSyncRequest.BiometricEntry();
        e.setDataType(dataType);
        e.setValue(value);
        e.setTimestamp(timestamp);
        return e;
    }
}
