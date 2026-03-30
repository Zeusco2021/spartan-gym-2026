package com.spartangoldengym.common.config;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    // Main topics
    public static final String WORKOUT_COMPLETED = "workout.completed";
    public static final String USER_ACHIEVEMENTS = "user.achievements";
    public static final String REAL_TIME_HEARTRATE = "real.time.heartrate";
    public static final String AI_RECOMMENDATIONS_REQUEST = "ai.recommendations.request";
    public static final String SOCIAL_INTERACTIONS = "social.interactions";
    public static final String NUTRITION_LOGS = "nutrition.logs";
    public static final String GYM_OCCUPANCY = "gym.occupancy";
    public static final String BOOKINGS_EVENTS = "bookings.events";

    // Dead Letter Queue topics
    public static final String DLQ_SUFFIX = ".dlq";
    public static final String WORKOUT_COMPLETED_DLQ = WORKOUT_COMPLETED + DLQ_SUFFIX;
    public static final String USER_ACHIEVEMENTS_DLQ = USER_ACHIEVEMENTS + DLQ_SUFFIX;
    public static final String REAL_TIME_HEARTRATE_DLQ = REAL_TIME_HEARTRATE + DLQ_SUFFIX;
    public static final String AI_RECOMMENDATIONS_REQUEST_DLQ = AI_RECOMMENDATIONS_REQUEST + DLQ_SUFFIX;
    public static final String SOCIAL_INTERACTIONS_DLQ = SOCIAL_INTERACTIONS + DLQ_SUFFIX;
    public static final String NUTRITION_LOGS_DLQ = NUTRITION_LOGS + DLQ_SUFFIX;
    public static final String GYM_OCCUPANCY_DLQ = GYM_OCCUPANCY + DLQ_SUFFIX;
    public static final String BOOKINGS_EVENTS_DLQ = BOOKINGS_EVENTS + DLQ_SUFFIX;
}
