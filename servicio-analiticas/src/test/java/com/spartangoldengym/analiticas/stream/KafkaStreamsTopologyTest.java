package com.spartangoldengym.analiticas.stream;

import com.spartangoldengym.analiticas.config.KafkaStreamsConfig;
import com.spartangoldengym.analiticas.dto.EngagementAggregation;
import com.spartangoldengym.analiticas.dto.PerformanceAggregation;
import com.spartangoldengym.analiticas.dto.WorkoutAggregation;
import com.spartangoldengym.common.config.KafkaTopics;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Kafka Streams topology using TopologyTestDriver.
 * Tests workout aggregations, performance metrics, and engagement metrics.
 */
class KafkaStreamsTopologyTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, String> workoutTopic;
    private TestInputTopic<String, String> heartrateTopic;
    private TestInputTopic<String, String> socialTopic;
    private TestInputTopic<String, String> nutritionTopic;

    @BeforeEach
    void setUp() {
        StreamsBuilder builder = new StreamsBuilder();
        KafkaStreamsConfig config = new KafkaStreamsConfig();

        // Build the topology
        config.workoutAggregationStream(builder);
        config.performanceAggregationStream(builder);
        config.engagementAggregationStream(builder);

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-analytics-streams");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());

        testDriver = new TopologyTestDriver(builder.build(), props);

        workoutTopic = testDriver.createInputTopic(
                KafkaTopics.WORKOUT_COMPLETED, new StringSerializer(), new StringSerializer());
        heartrateTopic = testDriver.createInputTopic(
                KafkaTopics.REAL_TIME_HEARTRATE, new StringSerializer(), new StringSerializer());
        socialTopic = testDriver.createInputTopic(
                KafkaTopics.SOCIAL_INTERACTIONS, new StringSerializer(), new StringSerializer());
        nutritionTopic = testDriver.createInputTopic(
                KafkaTopics.NUTRITION_LOGS, new StringSerializer(), new StringSerializer());
    }

    @AfterEach
    void tearDown() {
        if (testDriver != null) {
            testDriver.close();
        }
    }

    @Test
    void workoutAggregation_countsWorkoutsPerUser() {
        workoutTopic.pipeInput("user1", "{\"weight\":100,\"reps\":10,\"durationSeconds\":3600}");
        workoutTopic.pipeInput("user1", "{\"weight\":80,\"reps\":12,\"durationSeconds\":2700}");
        workoutTopic.pipeInput("user2", "{\"weight\":60,\"reps\":15,\"durationSeconds\":1800}");

        KeyValueStore<String, WorkoutAggregation> store =
                testDriver.getKeyValueStore(KafkaStreamsConfig.WORKOUT_AGGREGATION_STORE);

        WorkoutAggregation user1Agg = store.get("user1");
        assertNotNull(user1Agg);
        assertEquals(2, user1Agg.getWorkoutCount());

        WorkoutAggregation user2Agg = store.get("user2");
        assertNotNull(user2Agg);
        assertEquals(1, user2Agg.getWorkoutCount());
    }

    @Test
    void workoutAggregation_calculatesVolume() {
        // volume = weight * reps
        workoutTopic.pipeInput("user1", "{\"weight\":100,\"reps\":10,\"durationSeconds\":3600}");
        workoutTopic.pipeInput("user1", "{\"weight\":80,\"reps\":12,\"durationSeconds\":2700}");

        KeyValueStore<String, WorkoutAggregation> store =
                testDriver.getKeyValueStore(KafkaStreamsConfig.WORKOUT_AGGREGATION_STORE);

        WorkoutAggregation agg = store.get("user1");
        // 100*10 + 80*12 = 1000 + 960 = 1960
        assertEquals(1960.0, agg.getTotalVolume(), 0.01);
    }

    @Test
    void workoutAggregation_calculatesAverageDuration() {
        workoutTopic.pipeInput("user1", "{\"weight\":100,\"reps\":10,\"durationSeconds\":3600}");
        workoutTopic.pipeInput("user1", "{\"weight\":80,\"reps\":12,\"durationSeconds\":2400}");

        KeyValueStore<String, WorkoutAggregation> store =
                testDriver.getKeyValueStore(KafkaStreamsConfig.WORKOUT_AGGREGATION_STORE);

        WorkoutAggregation agg = store.get("user1");
        assertEquals(6000, agg.getTotalDurationSeconds());
        assertEquals(3000.0, agg.getAverageDurationSeconds(), 0.01);
    }

    @Test
    void performanceAggregation_tracksHeartrateStats() {
        heartrateTopic.pipeInput("user1", "{\"bpm\":120}");
        heartrateTopic.pipeInput("user1", "{\"bpm\":140}");
        heartrateTopic.pipeInput("user1", "{\"bpm\":100}");

        KeyValueStore<String, PerformanceAggregation> store =
                testDriver.getKeyValueStore(KafkaStreamsConfig.PERFORMANCE_AGGREGATION_STORE);

        PerformanceAggregation agg = store.get("user1");
        assertNotNull(agg);
        assertEquals(3, agg.getSampleCount());
        assertEquals(120.0, agg.getAverageHeartrate(), 0.01);
        assertEquals(140.0, agg.getHeartrateMax(), 0.01);
        assertEquals(100.0, agg.getHeartrateMin(), 0.01);
    }

    @Test
    void engagementAggregation_countsSocialInteractions() {
        socialTopic.pipeInput("user1", "{\"type\":\"like\"}");
        socialTopic.pipeInput("user1", "{\"type\":\"comment\"}");
        socialTopic.pipeInput("user2", "{\"type\":\"share\"}");

        KeyValueStore<String, EngagementAggregation> store =
                testDriver.getKeyValueStore(KafkaStreamsConfig.ENGAGEMENT_AGGREGATION_STORE);

        EngagementAggregation user1Agg = store.get("user1");
        assertNotNull(user1Agg);
        assertEquals(2, user1Agg.getSocialInteractionCount());

        EngagementAggregation user2Agg = store.get("user2");
        assertNotNull(user2Agg);
        assertEquals(1, user2Agg.getSocialInteractionCount());
    }

    @Test
    void engagementAggregation_countsNutritionLogs() {
        nutritionTopic.pipeInput("user1", "{\"food\":\"chicken\"}");
        nutritionTopic.pipeInput("user1", "{\"food\":\"rice\"}");
        nutritionTopic.pipeInput("user1", "{\"food\":\"salad\"}");

        KeyValueStore<String, EngagementAggregation> store =
                testDriver.getKeyValueStore(KafkaStreamsConfig.ENGAGEMENT_AGGREGATION_STORE);

        EngagementAggregation agg = store.get("user1");
        assertNotNull(agg);
        assertEquals(3, agg.getNutritionLogCount());
    }

    @Test
    void engagementAggregation_mergesSocialAndNutrition() {
        socialTopic.pipeInput("user1", "{\"type\":\"like\"}");
        nutritionTopic.pipeInput("user1", "{\"food\":\"chicken\"}");
        socialTopic.pipeInput("user1", "{\"type\":\"comment\"}");

        KeyValueStore<String, EngagementAggregation> store =
                testDriver.getKeyValueStore(KafkaStreamsConfig.ENGAGEMENT_AGGREGATION_STORE);

        EngagementAggregation agg = store.get("user1");
        assertNotNull(agg);
        assertEquals(2, agg.getSocialInteractionCount());
        assertEquals(1, agg.getNutritionLogCount());
    }

    @Test
    void workoutAggregation_handlesEmptyJson() {
        workoutTopic.pipeInput("user1", "{}");

        KeyValueStore<String, WorkoutAggregation> store =
                testDriver.getKeyValueStore(KafkaStreamsConfig.WORKOUT_AGGREGATION_STORE);

        WorkoutAggregation agg = store.get("user1");
        assertNotNull(agg);
        assertEquals(1, agg.getWorkoutCount());
        assertEquals(0.0, agg.getTotalVolume(), 0.01);
        assertEquals(0, agg.getTotalDurationSeconds());
    }
}
