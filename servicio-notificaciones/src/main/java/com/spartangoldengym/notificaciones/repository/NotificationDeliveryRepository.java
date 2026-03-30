package com.spartangoldengym.notificaciones.repository;

import com.spartangoldengym.notificaciones.model.NotificationDelivery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Repository for notification delivery records in DynamoDB.
 * Table: notification_delivery (PK=userId, SK=notificationId)
 * Validates: Requirement 22.6
 */
@Repository
public class NotificationDeliveryRepository {

    private static final Logger log = LoggerFactory.getLogger(NotificationDeliveryRepository.class);
    private static final String TABLE_NAME = "notification_delivery";

    private final DynamoDbTable<NotificationDelivery> table;

    public NotificationDeliveryRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.table = dynamoDbEnhancedClient.table(TABLE_NAME,
                TableSchema.fromBean(NotificationDelivery.class));
    }

    public void save(NotificationDelivery delivery) {
        table.putItem(delivery);
    }

    public NotificationDelivery findByUserIdAndNotificationId(String userId, String notificationId) {
        return table.getItem(Key.builder()
                .partitionValue(userId)
                .sortValue(notificationId)
                .build());
    }

    public List<NotificationDelivery> findByUserId(String userId) {
        return table.query(QueryConditional.keyEqualTo(
                        Key.builder().partitionValue(userId).build()))
                .items()
                .stream()
                .collect(Collectors.toList());
    }

    public List<NotificationDelivery> findPendingByStatus(String userId, String status) {
        return findByUserId(userId).stream()
                .filter(d -> status.equals(d.getStatus()))
                .collect(Collectors.toList());
    }
}
