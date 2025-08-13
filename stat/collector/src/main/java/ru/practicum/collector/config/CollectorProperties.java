package ru.practicum.collector.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "collector")
public class CollectorProperties {

    /** Kafka bootstrap servers, например: localhost:9092 */
    private String bootstrapServers;

    /** client.id продьюсера */
    private String clientId;

    /** Топик, куда шлём Avro-сообщения о действиях пользователей */
    private String userActions;
}