package ru.practicum.aggregator.config;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.aggregator.kafka.UserActionDeserializer;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Properties;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public Consumer<String, UserActionAvro> sensorConsumer(AggregatorProperties props) {
        Properties cfg = new Properties();

        // базовые настройки
        cfg.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                props.getBootstrapServers() != null ? props.getBootstrapServers() : "localhost:9092"
        );
        cfg.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                props.getGroupId() != null ? props.getGroupId() : "aggregator-group"
        );

        // десериализация
        cfg.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        cfg.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UserActionDeserializer.class.getName());

        // поведение чтения
        cfg.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        cfg.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new KafkaConsumer<>(cfg);
    }
}