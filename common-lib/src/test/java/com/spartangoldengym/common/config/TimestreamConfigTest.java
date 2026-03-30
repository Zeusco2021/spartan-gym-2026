package com.spartangoldengym.common.config;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TimestreamConfigTest {

    @Test
    void databaseNameIsSpartanMetrics() {
        assertEquals("spartan_metrics", TimestreamConfig.DATABASE_NAME);
    }

    @Test
    void allFourTablesAreDefined() {
        Set<String> tables = new HashSet<>(Arrays.asList(
                TimestreamConfig.TABLE_HEARTRATE_DATA,
                TimestreamConfig.TABLE_WORKOUT_METRICS,
                TimestreamConfig.TABLE_BIOMETRIC_DATA,
                TimestreamConfig.TABLE_PERFORMANCE_METRICS
        ));
        assertEquals(4, tables.size(), "Should define exactly 4 unique table names");
        assertTrue(tables.contains("heartrate_data"));
        assertTrue(tables.contains("workout_metrics"));
        assertTrue(tables.contains("biometric_data"));
        assertTrue(tables.contains("performance_metrics"));
    }

    @Test
    void qualifiedDatabaseNameIncludesEnvironment() {
        assertEquals("spartan_metrics_dev", TimestreamConfig.qualifiedDatabaseName("dev"));
        assertEquals("spartan_metrics_produccion", TimestreamConfig.qualifiedDatabaseName("produccion"));
    }

    @Test
    void dimensionConstantsAreNonEmpty() {
        assertFalse(TimestreamConfig.DIM_USER_ID.isEmpty());
        assertFalse(TimestreamConfig.DIM_SESSION_ID.isEmpty());
        assertFalse(TimestreamConfig.DIM_DEVICE_TYPE.isEmpty());
        assertFalse(TimestreamConfig.DIM_EXERCISE_ID.isEmpty());
        assertFalse(TimestreamConfig.DIM_MUSCLE_GROUP.isEmpty());
        assertFalse(TimestreamConfig.DIM_DATA_TYPE.isEmpty());
        assertFalse(TimestreamConfig.DIM_SOURCE.isEmpty());
        assertFalse(TimestreamConfig.DIM_SERVICE_ID.isEmpty());
        assertFalse(TimestreamConfig.DIM_ENDPOINT.isEmpty());
        assertFalse(TimestreamConfig.DIM_METHOD.isEmpty());
    }

    @Test
    void measureConstantsAreNonEmpty() {
        assertFalse(TimestreamConfig.MEASURE_BPM.isEmpty());
        assertFalse(TimestreamConfig.MEASURE_WEIGHT.isEmpty());
        assertFalse(TimestreamConfig.MEASURE_REPS.isEmpty());
        assertFalse(TimestreamConfig.MEASURE_VOLUME.isEmpty());
        assertFalse(TimestreamConfig.MEASURE_DURATION.isEmpty());
        assertFalse(TimestreamConfig.MEASURE_VALUE.isEmpty());
        assertFalse(TimestreamConfig.MEASURE_LATENCY.isEmpty());
        assertFalse(TimestreamConfig.MEASURE_STATUS_CODE.isEmpty());
    }
}
