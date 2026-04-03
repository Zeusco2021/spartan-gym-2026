package com.spartangoldengym.analiticas.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the JSON extraction helpers in KafkaStreamsConfig.
 */
class KafkaStreamsConfigTest {

    @Test
    void extractVolume_calculatesWeightTimesReps() {
        String json = "{\"weight\":100,\"reps\":10,\"durationSeconds\":3600}";
        assertEquals(1000.0, KafkaStreamsConfig.extractVolume(json), 0.01);
    }

    @Test
    void extractVolume_returnsZeroForMissingFields() {
        assertEquals(0.0, KafkaStreamsConfig.extractVolume("{}"), 0.01);
        assertEquals(0.0, KafkaStreamsConfig.extractVolume(null), 0.01);
    }

    @Test
    void extractDuration_returnsDurationSeconds() {
        String json = "{\"weight\":100,\"reps\":10,\"durationSeconds\":3600}";
        assertEquals(3600, KafkaStreamsConfig.extractDuration(json));
    }

    @Test
    void extractHeartrate_returnsBpm() {
        String json = "{\"bpm\":120.5}";
        assertEquals(120.5, KafkaStreamsConfig.extractHeartrate(json), 0.01);
    }

    @Test
    void extractJsonDouble_handlesDecimalValues() {
        String json = "{\"bpm\":72.5}";
        assertEquals(72.5, KafkaStreamsConfig.extractJsonDouble(json, "bpm"), 0.01);
    }

    @Test
    void extractJsonDouble_handlesIntegerValues() {
        String json = "{\"bpm\":120}";
        assertEquals(120.0, KafkaStreamsConfig.extractJsonDouble(json, "bpm"), 0.01);
    }

    @Test
    void extractJsonDouble_returnsZeroForMissingKey() {
        String json = "{\"other\":100}";
        assertEquals(0.0, KafkaStreamsConfig.extractJsonDouble(json, "bpm"), 0.01);
    }

    @Test
    void extractJsonLong_handlesValidValues() {
        String json = "{\"durationSeconds\":3600}";
        assertEquals(3600L, KafkaStreamsConfig.extractJsonLong(json, "durationSeconds"));
    }

    @Test
    void extractJsonLong_returnsZeroForNull() {
        assertEquals(0L, KafkaStreamsConfig.extractJsonLong(null, "key"));
    }
}
