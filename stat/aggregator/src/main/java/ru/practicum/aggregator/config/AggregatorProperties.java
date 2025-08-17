package ru.practicum.aggregator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aggregator")
public class AggregatorProperties {

    private String bootstrapServers;
    private String clientId;

    // Топики
    private String userActions;
    private String eventsSimilarity;

    // Прочее
    private Long pollTimeoutMs = 2000L;

    private String groupId;

}