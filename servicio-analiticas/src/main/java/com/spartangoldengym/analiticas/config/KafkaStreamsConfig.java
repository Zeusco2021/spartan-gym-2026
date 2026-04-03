package com.spartangoldengym.analiticas.config;

import com.spartangoldengym.analiticas.dto.EngagementAggregation;
import com.spartangoldengym.analiticas.dto.PerformanceAggregation;
import com.spartangoldengym.analiticas.dto.WorkoutAggregation;
import com.spartangoldengym.analiticas.stream.WorkoutAggregationSerde;
import com.spartangoldengym.analiticas.stream.PerformanceAggregationSerde;
import com.spartangoldengym.analiticas.stream.EngagementAggregationSerde;
import com.spartangoldengym.common.config.KafkaTopics;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

/**
 * Kafka Streams topology for real-time aggregations of workout data,
 * performance metrics, and engagement metrics.
 *
 * Requirement 11.4: Kafka Streams para agregaciones en tiempo real
 * de datos de entrenamiento y métricas de rendimiento.
 */
@Configuration
@EnableKafkaStreams
public class KafkaStreamsConfig {

    public static final String WORKOUT_AGGREGATION_STORE = "workout-aggregation-store";
    public static final String PERFORMANCE_AGGREGATION_STORE = "performance-aggregation-store";
    public static final String ENGAGEMENT_AGGREGATION_STORE = "engagement-aggregation-store";

    @Bean
    public KStream<String, String> workoutAggregationStream(StreamsBuilder streamsBuilder) {
        KStream<String, String> workoutStream = streamsBuilder.stream(
                KafkaTopics.WORKOUT_COMPLETED,
                Consumed.with(Serdes.String(), Serdes.String())
        );

        workoutStream.groupByKey()
                .aggregate(
                        WorkoutAggregation::new,
                        (userId, eventJson, aggregation) -> {
                            double volume = extractVolume(eventJson);
                            long duration = extractDuration(eventJson);
                            return aggregation.add(volume, duration);
                        },
                        Materialized.<String, WorkoutAggregation, KeyValueStore<Bytes, byte[]>>as(WORKOUT_AGGREGATION_STORE)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(new WorkoutAggregationSerde())
                );

        return workoutStream;
    }

    @Bean
    public KStream<String, String> performanceAggregationStream(StreamsBuilder streamsBuilder) {
        KStream<String, String> heartrateStream = streamsBuilder.stream(
                KafkaTopics.REAL_TIME_HEARTRATE,
                Consumed.with(Serdes.String(), Serdes.String())
        );

        heartrateStream.groupByKey()
                .aggregate(
                        PerformanceAggregation::new,
                        (userId, eventJson, aggregation) -> {
                            double bpm = extractHeartrate(eventJson);
                            return aggregation.addHeartrate(bpm);
                        },
                        Materialized.<String, PerformanceAggregation, KeyValueStore<Bytes, byte[]>>as(PERFORMANCE_AGGREGATION_STORE)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(new PerformanceAggregationSerde())
                );

        return heartrateStream;
    }

    @Bean
    public KStream<String, String> engagementAggregationStream(StreamsBuilder streamsBuilder) {
        KStream<String, String> socialStream = streamsBuilder.stream(
                KafkaTopics.SOCIAL_INTERACTIONS,
                Consumed.with(Serdes.String(), Serdes.String())
        );

        KStream<String, String> nutritionStream = streamsBuilder.stream(
                KafkaTopics.NUTRITION_LOGS,
                Consumed.with(Serdes.String(), Serdes.String())
        );

        // Tag social events with "social:" prefix, nutrition with "nutrition:"
        KStream<String, String> taggedSocial = socialStream.mapValues(v -> "social:" + v);
        KStream<String, String> taggedNutrition = nutritionStream.mapValues(v -> "nutrition:" + v);

        KStream<String, String> merged = taggedSocial.merge(taggedNutrition);

        merged.groupByKey()
                .aggregate(
                        EngagementAggregation::new,
                        (userId, taggedEvent, aggregation) -> {
                            if (taggedEvent.startsWith("social:")) {
                                return aggregation.addSocialInteraction();
                            } else {
                                return aggregation.addNutritionLog();
                            }
                        },
                        Materialized.<String, EngagementAggregation, KeyValueStore<Bytes, byte[]>>as(ENGAGEMENT_AGGREGATION_STORE)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(new EngagementAggregationSerde())
                );

        return merged;
    }

    static double extractVolume(String eventJson) {
        double weight = extractJsonDouble(eventJson, "weight");
        long reps = extractJsonLong(eventJson, "reps");
        return weight * reps;
    }

    static long extractDuration(String eventJson) {
        return extractJsonLong(eventJson, "durationSeconds");
    }

    static double extractHeartrate(String eventJson) {
        return extractJsonDouble(eventJson, "bpm");
    }

    /**
     * Simple JSON double extraction without external dependencies.
     * Looks for "key":value pattern in JSON string.
     */
    static double extractJsonDouble(String json, String key) {
        if (json == null) return 0.0;
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx < 0) return 0.0;
        int start = idx + search.length();
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.' || json.charAt(end) == '-')) {
            end++;
        }
        if (start == end) return 0.0;
        try {
            return Double.parseDouble(json.substring(start, end));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Simple JSON long extraction without external dependencies.
     */
    static long extractJsonLong(String json, String key) {
        if (json == null) return 0L;
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx < 0) return 0L;
        int start = idx + search.length();
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
            end++;
        }
        if (start == end) return 0L;
        try {
            return Long.parseLong(json.substring(start, end));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
