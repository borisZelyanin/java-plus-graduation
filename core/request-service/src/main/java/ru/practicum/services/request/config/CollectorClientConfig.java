package ru.practicum.services.request.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.wrap.grpc.client.stats.CollectorClient;

@Configuration
public class CollectorClientConfig {

    @Bean
    public CollectorClient collectorClient() {
        return new CollectorClient();
    }
}
