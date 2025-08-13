package ru.practicum.aggregator.config;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.aggregator.kafka.GeneralKafkaSerializer;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.util.Properties;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public Producer<String, EventSimilarityAvro> eventSimilarityProducer(AggregatorProperties props) {
        final String bootstrap = props.getBootstrapServers() != null
                ? props.getBootstrapServers() : "localhost:9092";
        final String clientId = props.getClientId() != null
                ? props.getClientId() : "aggregator-producer";

        Properties cfg = new Properties();
        cfg.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        cfg.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);

        // сериализаторы
        cfg.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        cfg.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralKafkaSerializer.class.getName());
        // если твоему сериализатору нужен таргет-тип
        cfg.put("general.serializer.targetType", EventSimilarityAvro.class.getName());

        // надёжность/производительность (по желанию)
        cfg.put(ProducerConfig.ACKS_CONFIG, "all");
        cfg.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        cfg.put(ProducerConfig.RETRIES_CONFIG, 3);
        cfg.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        cfg.put(ProducerConfig.BATCH_SIZE_CONFIG, 32768);

        return new KafkaProducer<>(cfg);
    }
}