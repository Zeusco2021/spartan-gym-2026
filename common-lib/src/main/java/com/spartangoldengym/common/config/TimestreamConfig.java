package com.spartangoldengym.common.config;

/**
 * Amazon Timestream database and table name constants.
 * Definitions match the Terraform module in infrastructure/terraform/modules/timestream/.
 *
 * Validates: Requirement 12.3
 */
public final class TimestreamConfig {

    private TimestreamConfig() {
    }

    // --- Database name (without environment suffix) ---
    public static final String DATABASE_NAME = "spartan_metrics";

    // --- Table names ---
    public static final String TABLE_HEARTRATE_DATA = "heartrate_data";
    public static final String TABLE_WORKOUT_METRICS = "workout_metrics";
    public static final String TABLE_BIOMETRIC_DATA = "biometric_data";
    public static final String TABLE_PERFORMANCE_METRICS = "performance_metrics";

    // --- Dimension names (shared across tables) ---
    public static final String DIM_USER_ID = "userId";
    public static final String DIM_SESSION_ID = "sessionId";
    public static final String DIM_DEVICE_TYPE = "deviceType";
    public static final String DIM_EXERCISE_ID = "exerciseId";
    public static final String DIM_MUSCLE_GROUP = "muscleGroup";
    public static final String DIM_DATA_TYPE = "dataType";
    public static final String DIM_SOURCE = "source";
    public static final String DIM_SERVICE_ID = "serviceId";
    public static final String DIM_ENDPOINT = "endpoint";
    public static final String DIM_METHOD = "method";

    // --- Measure names ---
    public static final String MEASURE_BPM = "bpm";
    public static final String MEASURE_WEIGHT = "weight";
    public static final String MEASURE_REPS = "reps";
    public static final String MEASURE_VOLUME = "volume";
    public static final String MEASURE_DURATION = "duration";
    public static final String MEASURE_VALUE = "value";
    public static final String MEASURE_LATENCY = "latency";
    public static final String MEASURE_STATUS_CODE = "statusCode";

    /**
     * Returns the fully-qualified database name with environment suffix.
     * Format: spartan_metrics_{env}
     */
    public static String qualifiedDatabaseName(String env) {
        return DATABASE_NAME + "_" + env;
    }
}
