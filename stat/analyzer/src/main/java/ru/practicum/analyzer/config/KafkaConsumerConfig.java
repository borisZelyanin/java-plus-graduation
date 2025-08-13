package ru.practicum.analyzer.config;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.*;
import ru.practicum.analyzer.kafka.GeneralKafkaSerializer;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;


import java.util.Properties;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public Consumer<String, UserActionAvro> userActionsConsumer(AnalyzerProperties props) {
        var cfg = base(props);
        cfg.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, GeneralKafkaSerializer.class.getName());
        cfg.put("general.deserializer.targetType", UserActionAvro.class.getName());
        return new KafkaConsumer<>(cfg);
    }

    @Bean
    public Consumer<String, EventSimilarityAvro> similaritiesConsumer(AnalyzerProperties props) {
        var cfg = base(props);
        cfg.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, GeneralKafkaSerializer.class.getName());
        cfg.put("general.deserializer.targetType", EventSimilarityAvro.class.getName());
        return new KafkaConsumer<>(cfg);
    }

    private Properties base(AnalyzerProperties props) {
        Properties p = new Properties();
        p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, props.getKafka().getBootstrapServers());
        p.put(ConsumerConfig.GROUP_ID_CONFIG, props.getKafka().getGroupId());
        p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        p.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return p;
    }
}