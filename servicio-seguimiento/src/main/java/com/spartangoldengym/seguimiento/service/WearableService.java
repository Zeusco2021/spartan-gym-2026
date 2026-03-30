package com.spartangoldengym.seguimiento.service;

import com.spartangoldengym.common.config.KafkaTopics;
import com.spartangoldengym.seguimiento.dto.*;
import com.spartangoldengym.seguimiento.model.BiometricData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages wearable device connections and biometric data synchronization.
 * Encryption note: TLS 1.3 in transit handled at infrastructure level,
 * AES-256 at rest handled by AWS (Timestream server-side encryption).
 *
 * Validates: Requirements 8.1, 8.2, 8.3, 8.5, 8.6
 */
@Service
public class WearableService {

    private static final Logger log = LoggerFactory.getLogger(WearableService.class);
    private static final Set<String> SUPPORTED_DEVICES = new HashSet<>(
            Arrays.asList("apple_watch", "fitbit", "garmin"));
    private static final Set<String> VALID_DATA_TYPES = new HashSet<>(
            Arrays.asList("heart_rate", "steps", "calories", "sleep"));

    // In-memory store for wearable connections (stub for DynamoDB)
    private final Map<String, WearableConnectResponse> connections = new ConcurrentHashMap<>();

    private final TimestreamMetricsService timestreamMetrics;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public WearableService(TimestreamMetricsService timestreamMetrics,
                           KafkaTemplate<String, String> kafkaTemplate) {
        this.timestreamMetrics = timestreamMetrics;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Connect/pair a wearable device for a user.
     * Validates: Requirement 8.2
     */
    public WearableConnectResponse connectWearable(WearableConnectRequest request) {
        if (request.getUserId() == null || request.getUserId().isEmpty()) {
            throw new IllegalArgumentException("userId is required");
        }
        if (request.getDeviceType() == null || !SUPPORTED_DEVICES.contains(request.getDeviceType())) {
            throw new IllegalArgumentException(
                    "Unsupported device type. Supported: " + SUPPORTED_DEVICES);
        }

        WearableConnectResponse response = new WearableConnectResponse();
        response.setConnectionId(UUID.randomUUID().toString());
        response.setUserId(request.getUserId());
        response.setDeviceType(request.getDeviceType());
        response.setDeviceId(request.getDeviceId());
        response.setStatus("connected");
        response.setConnectedAt(Instant.now());

        connections.put(response.getConnectionId(), response);
        log.info("Wearable connected: connectionId={}, userId={}, device={}",
                response.getConnectionId(), request.getUserId(), request.getDeviceType());
        return response;
    }

    /**
     * Sync pending biometric data from a wearable device.
     * Handles offline-collected data that is synced when connection is restored.
     * Heart rate data is also published to Kafka for real-time processing.
     *
     * Validates: Requirements 8.2, 8.3, 8.5
     */
    public WearableSyncResponse syncData(WearableSyncRequest request) {
        if (request.getUserId() == null || request.getUserId().isEmpty()) {
            throw new IllegalArgumentException("userId is required");
        }
        if (request.getData() == null || request.getData().isEmpty()) {
            throw new IllegalArgumentException("data must contain at least one entry");
        }

        List<BiometricData> biometricDataPoints = request.getData().stream()
                .filter(entry -> VALID_DATA_TYPES.contains(entry.getDataType()))
                .map(entry -> new BiometricData(
                        request.getUserId(),
                        entry.getDataType(),
                        entry.getValue(),
                        request.getDeviceType() != null ? request.getDeviceType() : "unknown",
                        entry.getTimestamp() != null ? entry.getTimestamp() : Instant.now()))
                .collect(Collectors.toList());

        // Store in Timestream with configurable retention
        timestreamMetrics.recordBiometricDataBatch(biometricDataPoints);

        // Publish heart rate entries to Kafka for real-time processing
        for (BiometricData data : biometricDataPoints) {
            if ("heart_rate".equals(data.getDataType())) {
                String payload = String.format(
                        "{\"userId\":\"%s\",\"bpm\":%.0f,\"deviceType\":\"%s\",\"timestamp\":\"%s\"}",
                        data.getUserId(), data.getValue(), data.getSource(), data.getTimestamp());
                kafkaTemplate.send(KafkaTopics.REAL_TIME_HEARTRATE, data.getUserId(), payload);
            }
        }

        WearableSyncResponse response = new WearableSyncResponse();
        response.setUserId(request.getUserId());
        response.setRecordsSynced(biometricDataPoints.size());
        response.setSyncedAt(Instant.now());

        log.info("Wearable sync completed: userId={}, records={}", request.getUserId(), biometricDataPoints.size());
        return response;
    }

    /** Visible for testing. */
    Map<String, WearableConnectResponse> getConnections() {
        return Collections.unmodifiableMap(connections);
    }
}
