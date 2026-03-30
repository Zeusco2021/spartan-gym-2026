package com.spartangoldengym.common.config;

/**
 * DynamoDB table names and key schema constants used across microservices.
 * Table definitions match the Terraform module in infrastructure/terraform/modules/dynamodb/.
 *
 * Validates: Requirement 12.2
 */
public final class DynamoDbTables {

    private DynamoDbTables() {
    }

    // --- Table names (without environment prefix) ---

    public static final String WORKOUT_SESSIONS = "workout_sessions";
    public static final String WORKOUT_SETS = "workout_sets";
    public static final String USER_ACHIEVEMENTS = "user_achievements";
    public static final String USER_PREFERENCES = "user_preferences";
    public static final String MESSAGES = "messages";
    public static final String CONVERSATIONS = "conversations";
    public static final String NOTIFICATION_DELIVERY = "notification_delivery";

    // --- Key attribute names ---

    // workout_sessions: PK=userId, SK=sessionId
    public static final String WORKOUT_SESSIONS_PK = "userId";
    public static final String WORKOUT_SESSIONS_SK = "sessionId";

    // workout_sets: PK=sessionId, SK=setId
    public static final String WORKOUT_SETS_PK = "sessionId";
    public static final String WORKOUT_SETS_SK = "setId";

    // user_achievements: PK=userId, SK=achievementId
    public static final String USER_ACHIEVEMENTS_PK = "userId";
    public static final String USER_ACHIEVEMENTS_SK = "achievementId";

    // user_preferences: PK=userId, SK=preferenceKey
    public static final String USER_PREFERENCES_PK = "userId";
    public static final String USER_PREFERENCES_SK = "preferenceKey";

    // messages: PK=conversationId, SK=messageId
    public static final String MESSAGES_PK = "conversationId";
    public static final String MESSAGES_SK = "messageId";

    // conversations: PK=userId, SK=conversationId
    public static final String CONVERSATIONS_PK = "userId";
    public static final String CONVERSATIONS_SK = "conversationId";

    // notification_delivery: PK=userId, SK=notificationId
    public static final String NOTIFICATION_DELIVERY_PK = "userId";
    public static final String NOTIFICATION_DELIVERY_SK = "notificationId";

    /**
     * Returns the fully-qualified table name with environment prefix.
     * Format: spartan-gym-{env}-{tableName}
     */
    public static String qualifiedName(String env, String tableName) {
        return "spartan-gym-" + env + "-" + tableName;
    }
}
