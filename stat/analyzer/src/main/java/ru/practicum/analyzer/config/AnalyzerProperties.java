package ru.practicum.analyzer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "analyzer")
public class AnalyzerProperties {
    private KafkaProps kafka = new KafkaProps();
    private LogicProps logic = new LogicProps();

    @Data
    public static class KafkaProps {
        private String bootstrapServers = "localhost:9092";
        private String groupId = "analyzer-group";
        private String topicUserActions = "stats.user-actions.v1";
        private String topicSimilarities = "stats.events-similarity.v1";
    }

    @Data
    public static class LogicProps {
        /** сколько последних взаимодействий пользователя брать как «источники» */
        private int recentN = 50;
        /** сколько соседей (сильнее всего похожих) учитывать при предсказании оценки */
        private int kNeighbors = 20;
    }
}