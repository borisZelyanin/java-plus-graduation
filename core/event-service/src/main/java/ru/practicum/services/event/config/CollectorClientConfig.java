package ru.practicum.services.event.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.wrap.grpc.client.stats.AnalyzerClient;
import ru.practicum.wrap.grpc.client.stats.CollectorClient;

@Configuration
public class CollectorClientConfig {

    @Bean
    public CollectorClient collectorClient() {
        return new CollectorClient();
    }

    @Bean
    public AnalyzerClient analyzerClient() {
        return new AnalyzerClient();
    }
}
