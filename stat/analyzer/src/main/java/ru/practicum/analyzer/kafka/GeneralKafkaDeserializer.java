// src/main/java/ru/practicum/analyzer/kafka/GeneralKafkaDeserializer.java
package ru.practicum.analyzer.kafka;

import org.apache.avro.Schema;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.serialization.Deserializer;

import java.lang.reflect.Method;
import java.util.Map;

public class GeneralKafkaDeserializer<T> implements Deserializer<T> {

    public static final String TARGET_TYPE_PROP = "general.deserializer.targetType";

    private Class<T> targetType;
    private Schema schema;

    @SuppressWarnings("unchecked")
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        Object clazzName = configs.get(TARGET_TYPE_PROP);
        if (clazzName == null) {
            throw new IllegalArgumentException(
                    "Missing required property: " + TARGET_TYPE_PROP + " (FQN of Avro SpecificRecord class)");
        }
        try {
            this.targetType = (Class<T>) Class.forName(clazzName.toString());
            // вызов статического метода getClassSchema() у Avro SpecificRecord
            Method m = targetType.getMethod("getClassSchema");
            this.schema = (Schema) m.invoke(null);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to init GeneralKafkaDeserializer for " + clazzName, e);
        }
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null || data.length == 0) return null;
        try {
            DatumReader<T> reader = new SpecificDatumReader<>(schema);
            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            return reader.read(null, decoder);
        } catch (Exception e) {
            throw new RuntimeException("Avro deserialization failed for topic=" + topic, e);
        }
    }

    @Override
    public void close() { /* no-op */ }
}