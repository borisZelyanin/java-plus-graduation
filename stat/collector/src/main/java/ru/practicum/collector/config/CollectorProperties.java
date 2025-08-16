package ru.practicum.collector.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "collector")
public class CollectorProperties {

    /** Топик, куда шлём Avro-сообщения о действиях пользователей */
    private String topicUserActions;
}