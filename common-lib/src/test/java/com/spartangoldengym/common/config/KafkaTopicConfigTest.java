package com.spartangoldengym.common.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KafkaTopicConfigTest {

    private KafkaTopicConfig config;

    @BeforeEach
    void setUp() {
        config = new KafkaTopicConfig();
    }

    @Test
    void mainTopicsHaveCorrectPartitions() {
        Map<String, Integer> expected = new HashMap<>();
        expected.put(KafkaTopics.WORKOUT_COMPLETED, 20);
        expected.put(KafkaTopics.USER_ACHIEVEMENTS, 10);
        expected.put(KafkaTopics.REAL_TIME_HEARTRATE, 50);
        expected.put(KafkaTopics.AI_RECOMMENDATIONS_REQUEST, 15);
        expected.put(KafkaTopics.SOCIAL_INTERACTIONS, 20);
        expected.put(KafkaTopics.NUTRITION_LOGS, 20);
        expected.put(KafkaTopics.GYM_OCCUPANCY, 10);
        expected.put(KafkaTopics.BOOKINGS_EVENTS, 10);

        Map<String, NewTopic> topics = buildMainTopicMap();

        for (Map.Entry<String, Integer> entry : expected.entrySet()) {
            NewTopic topic = topics.get(entry.getKey());
            assertNotNull(topic, "Topic " + entry.getKey() + " should exist");
            assertEquals(entry.getValue().intValue(), topic.numPartitions(),
                    "Partition count for " + entry.getKey());
        }
    }

    @Test
    void heartrateAndOccupancyTopicsHave24hRetention() {
        NewTopic heartrate = config.realTimeHeartrateTopic();
        NewTopic occupancy = config.gymOccupancyTopic();

        assertEquals("86400000", heartrate.configs().get("retention.ms"),
                "real.time.heartrate should have 24h retention");
        assertEquals("86400000", occupancy.configs().get("retention.ms"),
                "gym.occupancy should have 24h retention");
    }

    @Test
    void standardTopicsHave7dRetention() {
        NewTopic workout = config.workoutCompletedTopic();
        NewTopic achievements = config.userAchievementsTopic();
        NewTopic ai = config.aiRecommendationsRequestTopic();
        NewTopic social = config.socialInteractionsTopic();
        NewTopic nutrition = config.nutritionLogsTopic();
        NewTopic bookings = config.bookingsEventsTopic();

        String sevenDays = "604800000";
        assertEquals(sevenDays, workout.configs().get("retention.ms"));
        assertEquals(sevenDays, achievements.configs().get("retention.ms"));
        assertEquals(sevenDays, ai.configs().get("retention.ms"));
        assertEquals(sevenDays, social.configs().get("retention.ms"));
        assertEquals(sevenDays, nutrition.configs().get("retention.ms"));
        assertEquals(sevenDays, bookings.configs().get("retention.ms"));
    }

    @Test
    void allDlqTopicsExistWithCorrectNaming() {
        Map<String, NewTopic> dlqs = buildDlqTopicMap();

        String[] mainNames = {
                KafkaTopics.WORKOUT_COMPLETED,
                KafkaTopics.USER_ACHIEVEMENTS,
                KafkaTopics.REAL_TIME_HEARTRATE,
                KafkaTopics.AI_RECOMMENDATIONS_REQUEST,
                KafkaTopics.SOCIAL_INTERACTIONS,
                KafkaTopics.NUTRITION_LOGS,
                KafkaTopics.GYM_OCCUPANCY,
                KafkaTopics.BOOKINGS_EVENTS
        };

        assertEquals(mainNames.length, dlqs.size(), "Should have one DLQ per main topic");

        for (String mainName : mainNames) {
            String dlqName = mainName + KafkaTopics.DLQ_SUFFIX;
            NewTopic dlq = dlqs.get(dlqName);
            assertNotNull(dlq, "DLQ topic " + dlqName + " should exist");
            assertEquals(3, dlq.numPartitions(), "DLQ partitions for " + dlqName);
        }
    }

    @Test
    void allTopicsHaveReplicationFactor3() {
        Map<String, NewTopic> all = new HashMap<>();
        all.putAll(buildMainTopicMap());
        all.putAll(buildDlqTopicMap());

        for (Map.Entry<String, NewTopic> entry : all.entrySet()) {
            assertEquals((short) 3, entry.getValue().replicationFactor(),
                    "Replication factor for " + entry.getKey());
        }
    }

    private Map<String, NewTopic> buildMainTopicMap() {
        Map<String, NewTopic> map = new HashMap<>();
        NewTopic[] topics = {
                config.workoutCompletedTopic(),
                config.userAchievementsTopic(),
                config.realTimeHeartrateTopic(),
                config.aiRecommendationsRequestTopic(),
                config.socialInteractionsTopic(),
                config.nutritionLogsTopic(),
                config.gymOccupancyTopic(),
                config.bookingsEventsTopic()
        };
        for (NewTopic t : topics) {
            map.put(t.name(), t);
        }
        return map;
    }

    private Map<String, NewTopic> buildDlqTopicMap() {
        Map<String, NewTopic> map = new HashMap<>();
        NewTopic[] dlqs = {
                config.workoutCompletedDlq(),
                config.userAchievementsDlq(),
                config.realTimeHeartrateDlq(),
                config.aiRecommendationsRequestDlq(),
                config.socialInteractionsDlq(),
                config.nutritionLogsDlq(),
                config.gymOccupancyDlq(),
                config.bookingsEventsDlq()
        };
        for (NewTopic t : dlqs) {
            map.put(t.name(), t);
        }
        return map;
    }
}
