package com.spartangoldengym.seguimiento.service;

import com.spartangoldengym.common.config.TimestreamConfig;
import com.spartangoldengym.seguimiento.model.BiometricData;
import com.spartangoldengym.seguimiento.model.WorkoutSession;
import com.spartangoldengym.seguimiento.model.WorkoutSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Stub service for storing metrics in Amazon Timestream.
 * Logs operations until real Timestream client is configured.
 *
 * Validates: Requirement 4.9, 8.3, 12.3
 */
@Service
public class TimestreamMetricsService {

    private static final Logger log = LoggerFactory.getLogger(TimestreamMetricsService.class);

    public void recordHeartRate(String userId, String sessionId, int bpm, String deviceType) {
        log.info("Timestream [{}]: heartrate userId={}, sessionId={}, bpm={}, device={}",
                TimestreamConfig.TABLE_HEARTRATE_DATA, userId, sessionId, bpm, deviceType);
    }

    public void recordWorkoutMetrics(WorkoutSession session, List<WorkoutSet> sets) {
        log.info("Timestream [{}]: workout metrics sessionId={}, sets={}, duration={}s, calories={}",
                TimestreamConfig.TABLE_WORKOUT_METRICS,
                session.getSessionId(), sets.size(),
                session.getTotalDurationSeconds(), session.getCaloriesBurned());
    }

    /**
     * Record biometric data from wearable sync into Timestream biometric_data table.
     * Validates: Requirements 8.3, 12.3
     */
    public void recordBiometricData(BiometricData data) {
        log.info("Timestream [{}]: biometric userId={}, type={}, value={}, source={}, ts={}",
                TimestreamConfig.TABLE_BIOMETRIC_DATA,
                data.getUserId(), data.getDataType(), data.getValue(),
                data.getSource(), data.getTimestamp());
    }

    /**
     * Record a batch of biometric data points from wearable sync.
     * Validates: Requirements 8.2, 8.3
     */
    public void recordBiometricDataBatch(List<BiometricData> dataPoints) {
        for (BiometricData data : dataPoints) {
            recordBiometricData(data);
        }
        log.info("Timestream [{}]: batch recorded {} biometric data points",
                TimestreamConfig.TABLE_BIOMETRIC_DATA, dataPoints.size());
    }
}
