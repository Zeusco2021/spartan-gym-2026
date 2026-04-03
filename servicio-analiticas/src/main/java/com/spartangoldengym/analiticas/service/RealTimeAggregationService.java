package com.spartangoldengym.analiticas.service;

import com.spartangoldengym.analiticas.config.KafkaStreamsConfig;
import com.spartangoldengym.analiticas.dto.EngagementAggregation;
import com.spartangoldengym.analiticas.dto.PerformanceAggregation;
import com.spartangoldengym.analiticas.dto.RealTimeMetricsResponse;
import com.spartangoldengym.analiticas.dto.WorkoutAggregation;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Service that queries Kafka Streams state stores to provide
 * real-time aggregated metrics.
 */
@Service
public class RealTimeAggregationService {

    private static final Logger log = LoggerFactory.getLogger(RealTimeAggregationService.class);

    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    public RealTimeAggregationService(StreamsBuilderFactoryBean streamsBuilderFactoryBean) {
        this.streamsBuilderFactoryBean = streamsBuilderFactoryBean;
    }

    public RealTimeMetricsResponse getRealTimeAggregations() {
        RealTimeMetricsResponse response = new RealTimeMetricsResponse();
        response.setTimestamp(Instant.now());

        try {
            response.setWorkoutsByUser(queryWorkoutStore());
        } catch (Exception e) {
            log.warn("Could not query workout aggregation store: {}", e.getMessage());
            response.setWorkoutsByUser(new HashMap<String, WorkoutAggregation>());
        }

        try {
            response.setPerformanceByUser(queryPerformanceStore());
        } catch (Exception e) {
            log.warn("Could not query performance aggregation store: {}", e.getMessage());
            response.setPerformanceByUser(new HashMap<String, PerformanceAggregation>());
        }

        try {
            response.setEngagementByUser(queryEngagementStore());
        } catch (Exception e) {
            log.warn("Could not query engagement aggregation store: {}", e.getMessage());
            response.setEngagementByUser(new HashMap<String, EngagementAggregation>());
        }

        return response;
    }

    private Map<String, WorkoutAggregation> queryWorkoutStore() {
        KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
        if (kafkaStreams == null) return new HashMap<String, WorkoutAggregation>();

        ReadOnlyKeyValueStore<String, WorkoutAggregation> store = kafkaStreams.store(
                StoreQueryParameters.fromNameAndType(
                        KafkaStreamsConfig.WORKOUT_AGGREGATION_STORE,
                        QueryableStoreTypes.keyValueStore()
                )
        );

        Map<String, WorkoutAggregation> result = new HashMap<String, WorkoutAggregation>();
        store.all().forEachRemaining(kv -> result.put(kv.key, kv.value));
        return result;
    }

    private Map<String, PerformanceAggregation> queryPerformanceStore() {
        KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
        if (kafkaStreams == null) return new HashMap<String, PerformanceAggregation>();

        ReadOnlyKeyValueStore<String, PerformanceAggregation> store = kafkaStreams.store(
                StoreQueryParameters.fromNameAndType(
                        KafkaStreamsConfig.PERFORMANCE_AGGREGATION_STORE,
                        QueryableStoreTypes.keyValueStore()
                )
        );

        Map<String, PerformanceAggregation> result = new HashMap<String, PerformanceAggregation>();
        store.all().forEachRemaining(kv -> result.put(kv.key, kv.value));
        return result;
    }

    private Map<String, EngagementAggregation> queryEngagementStore() {
        KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
        if (kafkaStreams == null) return new HashMap<String, EngagementAggregation>();

        ReadOnlyKeyValueStore<String, EngagementAggregation> store = kafkaStreams.store(
                StoreQueryParameters.fromNameAndType(
                        KafkaStreamsConfig.ENGAGEMENT_AGGREGATION_STORE,
                        QueryableStoreTypes.keyValueStore()
                )
        );

        Map<String, EngagementAggregation> result = new HashMap<String, EngagementAggregation>();
        store.all().forEachRemaining(kv -> result.put(kv.key, kv.value));
        return result;
    }
}
