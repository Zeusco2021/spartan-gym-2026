package com.spartangoldengym.analiticas.stream;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Base JSON Serde using simple string-based serialization.
 * Avoids external JSON library dependency by using a functional interface approach.
 */
public abstract class JsonSerde<T> implements Serde<T> {

    private final Serializer<T> serializer;
    private final Deserializer<T> deserializer;

    protected JsonSerde(SerializeFunction<T> serializeFn, DeserializeFunction<T> deserializeFn) {
        this.serializer = new Serializer<T>() {
            @Override
            public void configure(Map<String, ?> configs, boolean isKey) { }

            @Override
            public byte[] serialize(String topic, T data) {
                if (data == null) return null;
                return serializeFn.apply(data).getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public void close() { }
        };

        this.deserializer = new Deserializer<T>() {
            @Override
            public void configure(Map<String, ?> configs, boolean isKey) { }

            @Override
            public T deserialize(String topic, byte[] data) {
                if (data == null) return null;
                return deserializeFn.apply(new String(data, StandardCharsets.UTF_8));
            }

            @Override
            public void close() { }
        };
    }

    @Override
    public Serializer<T> serializer() { return serializer; }

    @Override
    public Deserializer<T> deserializer() { return deserializer; }

    @FunctionalInterface
    public interface SerializeFunction<T> {
        String apply(T value);
    }

    @FunctionalInterface
    public interface DeserializeFunction<T> {
        T apply(String json);
    }
}
