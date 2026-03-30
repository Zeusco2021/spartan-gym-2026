package com.spartangoldengym.notificaciones.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartangoldengym.notificaciones.model.NotificationPreference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository for notification preferences stored in DynamoDB user_preferences table.
 * Uses preferenceKey = "notification_preferences".
 */
@Repository
public class NotificationPreferenceRepository {

    private static final Logger log = LoggerFactory.getLogger(NotificationPreferenceRepository.class);
    private static final String TABLE_NAME = "user_preferences";
    private static final String PREFERENCE_KEY = "notification_preferences";

    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;
    private final ObjectMapper objectMapper;

    public NotificationPreferenceRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient,
                                            ObjectMapper objectMapper) {
        this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
        this.objectMapper = objectMapper;
    }

    public NotificationPreference findByUserId(String userId) {
        try {
            // In production: query user_preferences table with PK=userId, SK="notification_preferences"
            // and deserialize the value attribute into NotificationPreference
            log.debug("Fetching notification preferences for user {}", userId);
            // Return default preferences if not found
            return new NotificationPreference(userId);
        } catch (Exception e) {
            log.error("Error fetching preferences for user {}: {}", userId, e.getMessage());
            return new NotificationPreference(userId);
        }
    }

    public void save(NotificationPreference preference) {
        try {
            // In production: serialize preference to JSON and store in user_preferences table
            log.debug("Saving notification preferences for user {}", preference.getUserId());
        } catch (Exception e) {
            log.error("Error saving preferences for user {}: {}", preference.getUserId(), e.getMessage());
        }
    }
}
