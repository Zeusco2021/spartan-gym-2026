package com.spartangoldengym.common.config;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DynamoDbTablesTest {

    private static final String[] EXPECTED_TABLES = {
            "workout_sessions", "workout_sets", "user_achievements",
            "user_preferences", "messages", "conversations", "notification_delivery"
    };

    @Test
    void allSevenTablesAreDefined() {
        Set<String> tableNames = new HashSet<>(Arrays.asList(
                DynamoDbTables.WORKOUT_SESSIONS,
                DynamoDbTables.WORKOUT_SETS,
                DynamoDbTables.USER_ACHIEVEMENTS,
                DynamoDbTables.USER_PREFERENCES,
                DynamoDbTables.MESSAGES,
                DynamoDbTables.CONVERSATIONS,
                DynamoDbTables.NOTIFICATION_DELIVERY
        ));
        assertEquals(7, tableNames.size(), "Should define exactly 7 unique table names");
        for (String expected : EXPECTED_TABLES) {
            assertTrue(tableNames.contains(expected), "Missing table: " + expected);
        }
    }

    @Test
    void eachTableHasPartitionAndSortKeyDefined() {
        // Verify PK/SK constants exist for every table by checking field naming convention
        assertNotNull(DynamoDbTables.WORKOUT_SESSIONS_PK);
        assertNotNull(DynamoDbTables.WORKOUT_SESSIONS_SK);
        assertNotNull(DynamoDbTables.WORKOUT_SETS_PK);
        assertNotNull(DynamoDbTables.WORKOUT_SETS_SK);
        assertNotNull(DynamoDbTables.USER_ACHIEVEMENTS_PK);
        assertNotNull(DynamoDbTables.USER_ACHIEVEMENTS_SK);
        assertNotNull(DynamoDbTables.USER_PREFERENCES_PK);
        assertNotNull(DynamoDbTables.USER_PREFERENCES_SK);
        assertNotNull(DynamoDbTables.MESSAGES_PK);
        assertNotNull(DynamoDbTables.MESSAGES_SK);
        assertNotNull(DynamoDbTables.CONVERSATIONS_PK);
        assertNotNull(DynamoDbTables.CONVERSATIONS_SK);
        assertNotNull(DynamoDbTables.NOTIFICATION_DELIVERY_PK);
        assertNotNull(DynamoDbTables.NOTIFICATION_DELIVERY_SK);
    }

    @Test
    void keyAttributesMatchDesignSpec() {
        // workout_sessions: PK=userId, SK=sessionId
        assertEquals("userId", DynamoDbTables.WORKOUT_SESSIONS_PK);
        assertEquals("sessionId", DynamoDbTables.WORKOUT_SESSIONS_SK);

        // workout_sets: PK=sessionId, SK=setId
        assertEquals("sessionId", DynamoDbTables.WORKOUT_SETS_PK);
        assertEquals("setId", DynamoDbTables.WORKOUT_SETS_SK);

        // messages: PK=conversationId, SK=messageId
        assertEquals("conversationId", DynamoDbTables.MESSAGES_PK);
        assertEquals("messageId", DynamoDbTables.MESSAGES_SK);

        // conversations: PK=userId, SK=conversationId
        assertEquals("userId", DynamoDbTables.CONVERSATIONS_PK);
        assertEquals("conversationId", DynamoDbTables.CONVERSATIONS_SK);
    }

    @Test
    void qualifiedNameFormatsCorrectly() {
        assertEquals("spartan-gym-dev-workout_sessions",
                DynamoDbTables.qualifiedName("dev", DynamoDbTables.WORKOUT_SESSIONS));
        assertEquals("spartan-gym-produccion-messages",
                DynamoDbTables.qualifiedName("produccion", DynamoDbTables.MESSAGES));
    }
}
